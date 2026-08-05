# Part VI: Linux & Infrastructure

This part moves from a single Linux process to a multi-node Kubernetes
cluster, one chapter at a time. Each chapter follows the handbook's
standard structure (see `AUTHORING_GUIDE.md`), and the ten chapters are
ordered so each concept builds on the one before: commands and the file
system, then process supervision, then the two kernel primitives (cgroups,
namespaces) containers are built from, then the container and
orchestration tooling that consumes those primitives in production.

**Chapters:** [1. Linux Commands](#linux-commands) ·
[2. File System](#file-system) ·
[3. Process Management](#process-management) ·
[4. systemd](#systemd) ·
[5. cgroups](#cgroups) ·
[6. Namespaces](#namespaces) ·
[7. Docker](#docker) ·
[8. containerd](#containerd) ·
[9. Kubernetes](#kubernetes) ·
[10. Container Runtime Concepts](#container-runtime-concepts)

## Kernel-to-Cluster Roadmap

Modern "cloud native" infrastructure is a stack of narrow abstractions on
a handful of Linux kernel primitives. Each layer solves one problem and
hands a clean interface to the layer above — nothing above the kernel is
magic, and nothing below Kubernetes is Kubernetes-specific. Read the stack
bottom-up to learn it, top-down when debugging it:

```text
  Application process
        ^
        |  supervised / scheduled by
  Kubernetes control plane   (API server, etcd, scheduler, controllers)
        ^
        |  binds pods to nodes; talks to each node's kubelet
  CRI  -  Container Runtime Interface   (an API contract, not software)
        ^
        |  implemented by
  containerd / CRI-O   (high-level runtime: images, storage, lifecycle)
        ^
        |  shims out to, per container
  runc / crun / kata-runtime   (low-level OCI runtime: start one process)
        ^
        |  asks the kernel for
  namespaces (what a process can SEE) + cgroups (what it can USE)
        ^
        |  built from
  Linux kernel primitives: fork/clone/execve, VFS, CFS scheduler, signals
        ^
        |  supervised at boot / runtime by
  systemd (PID 1) + journald, on top of the file system
        ^
        |  inspected and operated with
  Linux commands: ps, ss, strace, journalctl, df, ip, tcpdump ...
```

Suggested reading paths:

- **Beginner** — Linux Commands → File System → Process Management: build a
  mental model of what a running program is on Linux.
- **Intermediate** — systemd → cgroups → Namespaces: how one process tree
  is supervised, metered, and isolated from the rest of the machine.
- **Advanced** — Docker → containerd → Kubernetes → Container Runtime
  Concepts: how those primitives assemble into a packaging format
  orchestrated across a fleet of machines.

Cross-references: kernel scheduling/memory management are covered in Part
II (Operating Systems); etcd's Raft protocol is covered in Part V
(Distributed Systems). This part focuses on how those building blocks
compose into the container and orchestration stack used in production.

---

# Linux Commands

## What is it?
The Linux command line is the set of text-based programs — shell builtins
(`cd`, `export`), coreutils (`ls`, `cp`, `grep`), and system tools (`ss`,
`systemctl`, `tcpdump`) — used through a shell such as `bash` or `zsh` to
operate a Linux system: files, processes, users, networking, and text.

## Why do we need it?
Servers are almost always headless. Commands give a scriptable, low-latency
interface for controlling and inspecting a machine over SSH — essential for
automation and fast incident response with no display attached.

## What problem does it solve?
It solves the need for a **uniform, composable control surface**: small
single-purpose tools chained with pipes, instead of one program trying to
anticipate every use case.

## History (only if useful)
Unix's 1970s philosophy — "do one thing well, compose through text
streams" — was standardized by POSIX and re-implemented as GNU coreutils.
Shells evolved from `sh` to `bash`/`zsh`, adding scripting and completion,
while the compose-with-pipes model never changed.

## How does it work?
A shell reads a line, expands variables and globs, splits it into
commands, and for each external command calls `fork()` followed by
`execve()`. A pipe (`|`) connects one process's stdout file descriptor to
the next process's stdin through an in-kernel buffer.

## Internal Working
- The shell tokenizes and expands (`$VAR`, `*`, `~`) *before* the target
  program sees its arguments.
- `fork()` clones the shell; child pages are copy-on-write, so cloning is
  cheap. `execve()` then replaces the child's memory image, same PID.
- `pipe()` creates connected file descriptors; `dup2()` rewires
  stdin/stdout so data flows kernel-side, never touching disk.
- The parent calls `wait()`/`waitpid()` on each child, exposing its exit
  status as `$?`.

## Step-by-Step Example
```bash
ps aux | grep '[j]ava' | awk '{print $2}' | xargs -r kill -TERM
```
1. The shell forks `ps`, `grep`, `awk`, and `xargs`, wiring each stdout to
   the next stdin.
2. `ps` lists processes; `grep` keeps lines containing "java" (the bracket
   trick keeps `grep` itself out of its own results).
3. `awk` extracts column 2, the PID.
4. `xargs` builds and runs `kill -TERM <pid>` once per PID received.

## Visualization
```text
shell
  |--fork--> ps aux ------------stdout--+
                                          |--pipe--> grep '[j]ava' --+
                                                                       |--pipe--> awk '{print $2}' --+
                                                                                                        |--pipe--> xargs kill -TERM
```

## Real-World Usage
- Incident triage: `top`, `journalctl -f`, `ss -tlnp`, `dmesg -T`.
- Log analysis: `grep`, `sed`, `awk`, `sort | uniq -c` for frequency counts.
- Network debugging: `curl -v`, `dig`, `tcpdump -ni any port 443`.

## Advantages
- Composable and scriptable; a handful of tools cover endless combinations.
- Low overhead; works over a thin SSH text session with no GUI needed.
- Nearly universal across distributions, containers, and cloud shells.

## Disadvantages
- Steep learning curve: cryptic flags and easy-to-miss quoting rules.
- Ad-hoc scripts are fragile (word-splitting, unguarded globbing).

## Trade-offs
Fast shell one-liners trade long-term readability and safety for speed of
authoring. Anything run more than a few times, or anything destructive,
should graduate to a reviewed script or a config-management tool.

## Alternatives
- PowerShell pipes typed objects instead of text, trading Unix-style
  universality for structure.
- Configuration-management tools (Ansible, Salt) wrap commands with
  idempotency, dry runs, and auditing.

## Performance Analysis
Pipes themselves are cheap — data moves through an in-kernel buffer, never
disk. The real cost is process creation: forking and exec'ing a new
process per loop iteration in a shell script is orders of magnitude slower
than using shell builtins or a single `awk`/`sed` pass over the same input.

## Complexity Analysis (if applicable)
Not an algorithm by itself, but the tools have known bounds: `grep` performs
close to linear-time substring search; `sort` is O(n log n); a naive shell
loop that re-invokes an external tool per line is O(n) *process creations*,
which dominates runtime for large inputs far more than any algorithmic term.

## Implementation
Find the top CPU-consuming process, then check what port it listens on:
```bash
top -bn1 | head -12
pid=$(ps -eo pid,%cpu,comm --sort=-%cpu | awk 'NR==2{print $1}')
ss -tlnp | grep "pid=$pid"
```

## Common Mistakes
- Parsing `ls` output instead of `find`/globs (breaks on spaces/newlines).
- Forgetting to quote variables, e.g. `rm -rf $dir/` when `$dir` is empty.
- Running destructive commands (`rm`, `dd`) on production without a dry run.

## Common Interview Questions

Q: What exactly happens when you type a command and press Enter in bash?

Answer:
The shell tokenizes and expands the line, checks aliases/builtins, then
for each external command forks itself and calls `execve()` in the child
to load the target program, while the parent waits and captures the exit
status.

Q: What is the difference between `>` and `>>`?

Answer:
`>` truncates the target file and writes from the beginning; `>>` opens the
file in append mode, so new output is added after whatever content already
exists.

Q: How would you find which process is listening on a given port?

Answer:
Run `ss -tlnp | grep :<port>` (or `lsof -i :<port>`), which reads the
kernel's socket tables and maps the listening socket back to its owning
PID and process name.

## Follow-up Interview Questions

Q: What is the difference between `kill <pid>` and `kill -9 <pid>`?

Answer:
Plain `kill` sends `SIGTERM`, a catchable, cooperative request to
terminate; `-9` sends `SIGKILL`, which the kernel enforces directly and a
process can neither catch nor block, skipping any application cleanup.

## How the Interviewer Thinks
Command-line questions check practical fluency, not memorized flags: does
the candidate reach for the right tool under pressure, and understand a
pipeline is real concurrent processes, not shell magic.

## Production Engineering Perspective
On production hosts, prefer read-only inspection first, use `tmux`/`screen`
for long-lived sessions that survive SSH drops, and keep an audit trail
(shell history, `auditd`) for anything that mutates state. Scripts that
will run more than once belong in version control, not a terminal history.

## Common Misconceptions
- "`ls` is a shell builtin" — it is usually an external binary
  (`/usr/bin/ls`); `cd` and `export` are the actual builtins.
- "`kill` always terminates immediately" — signals can be caught, ignored,
  or blocked, with the sole exceptions of `SIGKILL`/`SIGSTOP`.
- "Piping runs commands one after another" — piped commands run
  concurrently, streaming data as each stage produces it.

## Revision Notes
- Pipeline = real processes + kernel pipe buffers, not string glue.
- `fork()` copies, `execve()` replaces; `wait()` reaps and reports status.
- Quote variables; prefer builtins in hot loops; avoid parsing `ls`.
- `kill` default signal is `SIGTERM` (15); `SIGKILL` (9) cannot be caught.

## Summary
The command line exposes kernel functionality through small, composable
programs connected by pipes. Fluency here underpins every later chapter in
this part, since containers and Kubernetes are ultimately debugged with the
same primitives: processes, file descriptors, and signals.

---

# File System

## What is it?
A file system is the kernel-managed structure that organizes data on a
storage device (or in memory) into a hierarchy of files and directories,
each described by metadata called an inode.

## Why do we need it?
Applications need durable, named, permission-controlled storage that
survives reboots and can be shared safely between processes without
knowing the physical layout of the underlying disk.

## What problem does it solve?
It abstracts different physical media (disks, SSDs, network storage, RAM)
behind one POSIX API (`open`, `read`, `write`, `stat`), while internally
managing allocation, metadata, and concurrent access.

## History (only if useful)
The early Unix File System had no crash recovery, so `fsck` after a crash
could take hours. Journaling filesystems (ext3, XFS) added a write-ahead
log. Copy-on-write filesystems (Btrfs, ZFS) added snapshots and checksums;
OverlayFS later enabled Docker's layered images.

## How does it work?
The Virtual File System (VFS) is a kernel abstraction layer: syscalls like
`open()` are dispatched to whichever concrete filesystem driver (ext4,
xfs, overlay2, tmpfs) owns that path, so user space never needs
filesystem-specific code.

## Internal Working
- **Inode** — a metadata record (owner, permissions, size, timestamps,
  data pointers); it has no filename of its own.
- **Directory** — a special file mapping names to inode numbers; a hard
  link is simply another name pointing at the same inode.
- **Superblock** — per-filesystem metadata (size, block size, free lists).
- **Journal** — a circular log of pending changes, replayed on mount after
  an unclean shutdown for crash consistency.
- **Page cache** — reads/writes normally hit RAM first; `fsync()` forces
  data to durable storage.

## Step-by-Step Example
Creating a file with `open("/var/log/app.log", O_CREAT)`:
1. The VFS resolves the parent directory's inode.
2. The filesystem driver allocates a free inode and, lazily, data blocks.
3. A new directory entry links the name `app.log` to that inode number.
4. Writes land in the page cache; the journal records the metadata change;
   a background writeback thread (or an explicit `fsync`) flushes to disk.

## Visualization
```text
path lookup:  /var/log/app.log

  dir "/"    -> entry "var" -----> inode 101 (directory)
  dir "var"  -> entry "log" -----> inode 205 (directory)
  dir "log"  -> entry "app.log" -> inode 9931 (regular file)
                                       |
                                       v
                          [mode | owner | size | block pointers]
                                       |
                                       v
                             data blocks on the device
```

## Real-World Usage
- Choosing ext4/XFS for general workloads, overlay2 for Docker image
  layers, and tmpfs for ephemeral, high-speed scratch space.
- Diagnosing "No space left on device" despite `df` showing free bytes —
  usually inode exhaustion, or a deleted file still held open by a process.

## Advantages
- The POSIX permission model and API are portable and well understood.
- Journaling gives fast, predictable crash recovery versus a full `fsck`.
- Hierarchical naming scales to millions of files with cached lookups.

## Disadvantages
- Metadata and journal writes add overhead versus raw block access.
- Small-file workloads waste space and inodes relative to actual data.

## Trade-offs
Full data journaling protects file contents but roughly doubles write
traffic; metadata-only journaling (the common default) protects filesystem
structure but not in-flight file data. Copy-on-write filesystems trade
extra bookkeeping for cheap snapshots and built-in checksums.

## Alternatives
- Object storage (S3-style) for large-scale unstructured blobs, trading
  POSIX semantics for horizontal scalability.
- Network/distributed filesystems (NFS, Ceph, GlusterFS) for shared access
  across many hosts.

## Performance Analysis
Sequential I/O vastly outperforms random I/O on spinning disks, and remains
faster on SSDs due to fewer command round-trips. The page cache makes
repeated reads nearly free until memory pressure evicts pages. Useful
tools: `iostat -x` for device latency/throughput, `fio` for benchmarking,
`df -i` for inode pressure.

## Complexity Analysis (if applicable)
Path lookup costs O(depth) directory hops, but the dentry/inode cache makes
repeated lookups effectively O(1) amortized. Listing a directory costs
O(entries in that directory). Extent-based allocation (ext4, XFS) tracks
contiguous block ranges instead of per-block pointers, shrinking metadata
size and fragmentation compared with older block-list schemes.

## Implementation
```bash
stat /etc/hosts                 # inode number, size, timestamps
df -h /                         # space usage per mounted filesystem
df -i /                         # inode usage — a separate, often-missed limit
lsblk -f                        # block devices and their filesystem types
mount | column -t               # currently mounted filesystems and options
echo '/dev/sdb1 /data ext4 defaults,noatime 0 2' | sudo tee -a /etc/fstab
```

## Common Mistakes
- Expecting space back the instant `rm` runs on a file another process
  still has open (space frees only once the last file descriptor closes).
- Never checking inode usage separately from byte usage.

## Common Interview Questions

Q: What is an inode, and does it store the filename?

Answer:
An inode stores a file's metadata — owner, permissions, size, timestamps,
data pointers — but not its name. Names live in directory entries mapping
to inode numbers, so one file can have several hard-linked names.

Q: What is the difference between a hard link and a symbolic link?

Answer:
A hard link is a second directory entry to the same inode — cannot cross
filesystems, survives the original name being deleted. A symlink holds a
path string — can cross filesystems, but breaks if the target is
removed.

Q: What happens if you delete a file that a running process still has open?

Answer:
The directory entry is removed immediately, but the inode and its data
blocks are not freed until every open file descriptor referencing it is
closed, because the kernel tracks a reference count, not just the name.

## Follow-up Interview Questions

Q: How does OverlayFS, as used by Docker, combine multiple image layers into one filesystem view?

Answer:
It stacks read-only "lower" layers under one writable "upper" layer; reads
check upper-then-lower, and writing to a file only in a lower layer
triggers a "copy-up" into the upper layer, so lower layers stay
unmodified.

## How the Interviewer Thinks
This tests whether the candidate has a real mental model of storage, not
command memorization — can they explain why deleting a file does not
always free space, and tie that to a live incident.

## Production Engineering Perspective
Monitor both byte and inode usage; rotate logs before they fill the root
partition; keep application data off the OS disk where possible; remember
that container image layers add copy-up I/O cost on the first write to a
large file inherited from a lower layer.

## Common Misconceptions
- "Deleting a file always frees space instantly" — not if a process still
  holds it open.
- "Hard links work across partitions" — they cannot; inodes are only
  unique within a single filesystem.
- "More journaling is always strictly safer with no downside" — full data
  journaling costs write throughput in exchange for the added durability.

## Revision Notes
- Inode = metadata; directory entry = name-to-inode mapping.
- Space frees only when link count and open-fd count both reach zero.
- Journaling trades some throughput for crash consistency.
- Check `df -i` in addition to `df -h`.

## Summary
The filesystem hides physical storage behind inodes, directories, and a
journal, giving every process a consistent, crash-safe naming scheme. The
same inode/link/overlay model reappears later when Docker image layers are
covered in Chapter 7.

---

# Process Management

## What is it?
Process management is the kernel's mechanism for creating, scheduling,
tracking, and terminating running programs. Each process has its own
address space, file descriptor table, and execution state.

## Why do we need it?
A small number of CPU cores must run many independent programs that
should not corrupt each other's memory. The kernel needs a fair way to
multiplex hardware across them and reclaim resources when one exits.

## What problem does it solve?
It solves **safe concurrency on shared hardware**: isolating each program's
memory and file handles, deciding which runnable program gets the CPU next,
and providing a lifecycle (create, run, signal, reap) that other programs
(shells, supervisors) can control.

## History (only if useful)
Early Unix used simple cooperative scheduling. Linux's O(1) scheduler
(2.6) gave constant-time decisions but poor fairness; the Completely Fair
Scheduler (CFS, 2.6.23) replaced it, sharing CPU time proportionally.

## How does it work?
`fork()` (or the more general `clone()`) creates a new task by duplicating
the calling process; `execve()` then loads a new program image into that
task. The scheduler picks the next runnable task to run on a CPU; a timer
interrupt periodically forces a re-evaluation (preemption).

## Internal Working
- **Process states**: Running, Runnable (ready), Interruptible sleep
  (woken by a signal), Uninterruptible sleep ("D", waiting on I/O),
  Stopped, and Zombie (exited, awaiting reaping).
- **task_struct** holds PID, parent PID, file descriptors, memory map
  (`mm_struct`), signal handlers, and scheduling info.
- **CFS** keeps runnable tasks in a red-black tree keyed by virtual
  runtime, always picking the task that has run least so far.
- A **zombie** has exited but its exit status is uncollected; an
  **orphan**'s parent exited first, so it is reparented (to PID 1).

## Step-by-Step Example
```bash
sleep 100 &
kill -TERM $!
wait $!
```
1. The shell forks a child; the child `execve()`s `sleep`.
2. `kill -TERM` sends a terminate signal to that PID.
3. `sleep`'s default handler for `SIGTERM` ends the process; it becomes a
   zombie holding its exit status.
4. The shell's `wait` collects that status, and the kernel frees the slot.

## Visualization
```text
   fork()            execve()            SIGTERM           wait()
shell ----> [child: copy] ----> [child: sleep 100] ----> [zombie] ----> [reaped]
              (same code,        (new code, same PID)     (exited,       (slot
               new PID)                                    unclaimed)     freed)
```

## Real-World Usage
- `ps`, `top`, `htop` for a live process/resource view.
- `strace -p <pid>` to see the syscalls a running process is making.
- `/proc/<pid>/status`, `/proc/<pid>/fd` for deep inspection without
  attaching a debugger.

## Advantages
- Preemptive multitasking gives responsiveness and fairness under load.
- Separate address spaces isolate faults — one process crashing does not
  usually corrupt another's memory.
- The signal/wait lifecycle gives a uniform way to control any process.

## Disadvantages
- `fork()`/`execve()` has real overhead, though copy-on-write mitigates the
  memory-duplication cost.
- Zombies accumulate, and orphaned resources linger, if a parent never
  calls `wait()`.

## Trade-offs
Processes (isolated, heavier) trade memory/creation cost for safety versus
threads (shared memory, lighter, but one bad pointer can corrupt the whole
program) versus single-threaded event loops (no context-switch cost, but
harder application code for genuine parallel CPU work).

## Alternatives
- Threads or green threads/goroutines for lighter-weight concurrency within
  one address space.
- Event-driven single-process workers (e.g., nginx's worker model) for
  high-concurrency I/O-bound work.

## Performance Analysis
A context switch costs roughly one to a few microseconds, plus indirect
cost from cold CPU caches and TLB flushes. `vmstat` shows run-queue
length; cgroup CPU quotas can cause throttling that looks like a
performance bug but is actually a configured limit.

## Complexity Analysis (if applicable)
CFS selects and re-inserts the next runnable task in O(log n) using a
red-black tree keyed by accumulated virtual runtime, versus the older
scheduler's O(1) selection that sacrificed fairness for speed.

## Implementation
```bash
ps -eo pid,ppid,stat,pcpu,pmem,cmd --sort=-pcpu | head
cat /proc/<pid>/status | grep -E 'State|VmRSS'
strace -f -e trace=network -p <pid>
```

## Common Mistakes
- Not reaping children (ignoring `SIGCHLD`/`wait()`), leaking zombie slots.
- Killing the wrong PID after a fragile pattern match on `ps` output.
- Treating `kill -9` as a routine cleanup tool while ignoring the locks,
  temp files, or partial writes it leaves behind by skipping cleanup.

## Common Interview Questions

Q: What is the difference between a process and a thread?

Answer:
A process has its own isolated address space and file descriptor table; a
thread shares its process's address space with sibling threads, making
creation and context switching cheaper but crash isolation weaker.

Q: What is a zombie process, and how is it different from an orphan?

Answer:
A zombie has exited but still holds a process-table slot because its
parent hasn't called `wait()` yet. An orphan is the reverse: the parent
exited first, so the kernel reparents the still-running child, typically
to PID 1.

Q: How does `fork()` differ from `exec()`?

Answer:
`fork()` duplicates the calling process into a new process with a new
PID; `exec()` replaces the memory image with a new program, same PID —
usually combined: fork for a new process, exec to run something
different in it.

## Follow-up Interview Questions

Q: How would you debug a server with a high load average but low CPU utilization?

Answer:
Load average includes processes waiting on uninterruptible I/O, not just
CPU; check `ps -eo stat` for processes in "D" state, then look at disk
latency with `iostat -x` or trace which syscall is blocking with `strace`.

## How the Interviewer Thinks
This verifies OS fundamentals translate into real debugging ability: can
the candidate distinguish CPU-bound from I/O-bound stalls, and explain
what a supervisor must do to keep a process alive.

## Production Engineering Perspective
PID 1 (or a container's init) reaps orphaned processes; supervisors
restart crashed processes automatically; resource limits (`ulimit`,
cgroups — Chapter 5) prevent one runaway fork loop from taking down a
host.

## Common Misconceptions
- "`kill` always terminates immediately" — a caught or blocked signal can
  delay or prevent termination; only `SIGKILL` is unconditional.
- "Zombie processes consume significant resources" — a zombie holds only a
  small process-table entry, not memory or open files.
- "Thread creation is essentially free" — it is lighter than a process,
  but still allocates a stack and kernel scheduling state.

## Revision Notes
- States: Running, Runnable, Interruptible/Uninterruptible sleep, Stopped,
  Zombie.
- `fork()` copies (COW), `execve()` replaces, `wait()` reaps.
- CFS picks the task with the least virtual runtime via a red-black tree.
- "D" state usually means blocked on I/O, not CPU contention.

## Summary
Processes are the kernel's unit of isolation and scheduling: created by
`fork`/`clone`, given new code by `exec`, scheduled fairly by CFS, and
cleaned up through signals and `wait()`. The next three chapters build
supervision (systemd), resource limits (cgroups), and visibility isolation
(namespaces) on top of exactly this model.

---

# systemd

## What is it?
systemd is the modern Linux init system: the first user-space process
(PID 1) that boots the system, and the service manager that starts, stops,
supervises, and restarts every other service using declarative unit files.

## Why do we need it?
Booting a server means starting dozens of interdependent services
reliably and quickly, then keeping them running — too complex and error
prone for ad hoc shell scripts.

## What problem does it solve?
It replaces slow, serial, unsupervised SysV init scripts with a
dependency-aware, parallel startup system that also supervises services
for their entire lifetime and restarts them automatically on failure.

## History (only if useful)
SysV init ran numbered shell scripts serially per runlevel — simple but
slow and blind to failures. systemd (2010, Lennart Poettering) replaced
it and Upstart, becoming default on most distributions, absorbing
logging (journald), device management, and scheduling into one project.

## How does it work?
Unit files declare a service and its relationships (`Wants=`, `Requires=`,
`After=`, `Before=`). systemd builds a dependency graph from all units,
starts independent units in parallel, and uses cgroups to track every
process a service spawns so it can supervise and cleanly stop the whole
tree.

## Internal Working
- Unit types: `.service`, `.socket`, `.timer`, `.mount`, `.target` (a
  named grouping point, replacing runlevels).
- `Type=simple|forking|oneshot|notify` tells systemd how to decide a
  service has actually started.
- Socket activation: systemd can own a listening socket and start the
  backing service only on first connection, deferring startup cost.
- journald collects structured logs centrally; `Restart=on-failure` with
  backoff handles crash recovery.
- Every service's processes live in their own cgroup, so systemd can find
  and stop stragglers a plain `kill` would miss.

## Step-by-Step Example
Unit file at `/etc/systemd/system/myapp.service`:
```ini
[Unit]
Description=My application
After=network.target

[Service]
ExecStart=/usr/local/bin/myapp
Restart=on-failure
RestartSec=2
MemoryMax=512M

[Install]
WantedBy=multi-user.target
```
`systemctl enable --now myapp` links the unit into `multi-user.target`,
starts it immediately, places it in a cgroup with a 512M memory ceiling,
and restarts it two seconds after any non-zero exit.

## Visualization
```text
multi-user.target
     |-- requires --> network.target
     |-- wants ------> myapp.service --ExecStart--> myapp process
     |                                        |
     |                                        +--> tracked via its cgroup
     |-- wants ------> sshd.service
```

## Real-World Usage
- Writing unit files for internal services instead of hand-rolled scripts.
- `systemctl status|restart|enable`, `journalctl -u myapp -f` for logs.
- Timers (`.timer` + `.service` pairs) replacing cron with dependency
  awareness and logging.

## Advantages
- Parallel, dependency-ordered boot is much faster than serial SysV init.
- Built-in supervision and automatic restart on crash.
- Centralized logging (journald) and native resource limiting via cgroups.

## Disadvantages
- Broad scope (logging, devices, networking, timers) draws "does too much"
  criticism versus the Unix "do one thing" philosophy.
- Binary journal logs are less trivially greppable than plain text.

## Trade-offs
Declarative, supervised unit management trades some flexibility (arbitrary
shell logic at boot) for reliability, consistency, and built-in restart and
resource-limiting behavior across every service on the host.

## Alternatives
- OpenRC, runit, s6 — smaller, more Unix-philosophy-aligned init systems.
- supervisord — a common user-space process supervisor inside containers,
  where PID 1 duties are simpler than a full host boot.

## Performance Analysis
Parallel unit startup materially cuts boot time versus serial SysV
scripts. Socket activation defers a service's startup cost until needed.
journald's indexing adds modest overhead but enables fast queries.

## Complexity Analysis (if applicable)
Dependency resolution is a topological sort over the unit graph, O(V+E) in
the number of units and their declared relationships — the basis for
maximal safe parallelism during boot.

## Implementation
```bash
sudo systemctl daemon-reload      # required after editing any unit file
sudo systemctl enable --now myapp.service
systemctl status myapp.service
journalctl -u myapp.service --since "10 min ago"
systemctl list-dependencies multi-user.target
```

## Common Mistakes
- Forgetting `daemon-reload` after editing a unit file, so systemd keeps
  running against the old definition.
- Using `Type=simple` for a process that forks and exits its foreground
  parent, so systemd thinks the service died immediately.
- Editing packaged unit files directly instead of using a drop-in
  (`systemctl edit`), losing changes on the next package update.

## Common Interview Questions

Q: Why did systemd replace SysV init and Upstart on most distributions?

Answer:
It offers dependency-based parallel startup instead of strictly serial
scripts, plus first-class service supervision, automatic restarts, and
resource control via cgroups — all previously bolted on inconsistently by
each init script author.

Q: How do you make a service restart automatically after a crash?

Answer:
Set `Restart=on-failure` (or `always`) and optionally `RestartSec=` in the
unit's `[Service]` section; systemd relaunches the process after it exits
with a failure status, with a configurable delay to avoid tight crash
loops.

Q: What is the difference between journald and traditional syslog?

Answer:
journald stores structured, indexed, binary entries queryable by fields
like unit or priority (`journalctl -u`, `-p`); syslog appends plain text
lines — simpler to grep but slower to query and less structured.

## Follow-up Interview Questions

Q: A service shows "failed to start" immediately even though the process is clearly running — what is the likely cause?

Answer:
Usually a `Type=` mismatch — `Type=simple` used for a process that forks
to the background, so systemd treats the foreground exit as failure. Fix
with `Type=forking` plus `PIDFile=`, or `Type=notify` if supported.

## How the Interviewer Thinks
This gauges production operations maturity: can the candidate write a
robust unit file and debug a service that "won't start," rather than only
knowing `systemctl status` by rote.

## Production Engineering Perspective
Set `Restart=`, sane `RestartSec=` backoff, and resource ceilings
(`MemoryMax=`, `CPUQuota=`) in unit files; configure journald retention
(`SystemMaxUse=`) to avoid unbounded log growth.

## Common Misconceptions
- "systemd is just an init system" — it also manages logging, devices,
  mounts, and scheduled timers.
- "Disabling a service stops it" — `disable` only removes it from
  boot-time activation; a running instance keeps running until `stop`.
- "Manually killing a service's tracked PID equals `systemctl stop`" —
  systemd relies on the unit's cgroup to find every descendant process; a
  manual kill can leave stragglers systemd no longer expects.

## Revision Notes
- Units: service, socket, timer, mount, target (dependency-only grouping).
- `Wants`/`Requires` + `After`/`Before` define the dependency graph.
- Every service lives in its own cgroup — this is how systemd supervises
  reliably.
- Always `daemon-reload` after editing unit files.

## Summary
systemd turns "start these services in this order and keep them running"
into declarative, cgroup-backed configuration instead of imperative shell
scripts. The cgroup tracking it relies on for supervision is the exact
mechanism covered next, in Chapter 5.

---

# cgroups

## What is it?
Control groups (cgroups) are a Linux kernel feature that groups processes
and limits, accounts for, and prioritizes their use of resources: CPU,
memory, block I/O, and number of PIDs.

## Why do we need it?
Multiple workloads sharing one machine need enforced boundaries, or one
runaway process can starve every other process of CPU or memory.

## What problem does it solve?
It solves **resource isolation without separate machines**: cgroups give
the kernel a hard enforcement point for "how much of this resource may
this group of processes use," which is what lets a host safely run many
tenants or containers.

## History (only if useful)
cgroups shipped in Linux 2.6.24 (2007), contributed by Google from its
internal Borg work. The original design ("v1") gave each controller its
own hierarchy — flexible but confusing. cgroups v2 (2016, now default)
unifies all controllers under one hierarchy per group.

## How does it work?
cgroups are exposed as a pseudo-filesystem, `cgroupfs`, typically mounted
at `/sys/fs/cgroup`. Creating a directory there creates a cgroup; writing a
PID into its `cgroup.procs` file joins that process — and, going forward,
its children — to the group; writing to controller files like
`memory.max` sets the enforced limit.

## Internal Working
- **v1**: one hierarchy per controller (`cpu`, `memory`, `blkio`); a
  process can sit in different branches per resource.
- **v2**: a single unified hierarchy; controllers enable per subtree via
  `cgroup.subtree_control`, avoiding v1's cross-controller confusion.
- **memory** controller tracks page usage per group; exceeding `memory.max`
  triggers reclaim, then a cgroup-scoped OOM kill if reclaim fails.
- **cpu** controller enforces a hard quota/period (`cpu.max`) or relative
  share (`cpu.weight`); **io** throttles bytes/IOPS per group.

## Step-by-Step Example
```bash
docker run --memory=512m --cpus="1" myimage
```
1. containerd/runc creates a new cgroup for the container.
2. It writes `memory.max=536870912` and a `cpu.max` quota equal to one
   full CPU.
3. The kernel enforces both continuously: exceeding the memory ceiling
   triggers the cgroup-scoped OOM killer; exceeding the CPU quota throttles
   the group instead of just running slower.

## Visualization
```text
/sys/fs/cgroup/
  kubepods.slice/
    pod-abc123.slice/
      container-xyz/
        cgroup.procs   <- PIDs in this container
        memory.max     <- hard memory ceiling
        cpu.max        <- quota/period
        memory.current <- live usage, read by monitoring
```

## Real-World Usage
- Kubernetes `resources.requests/limits` compile directly into cgroup
  settings on the node.
- systemd places every service in its own cgroup
  (`system.slice/myapp.service`).
- Monitoring: `memory.current`, `cpu.stat` (including `nr_throttled`) feed
  cAdvisor/Prometheus dashboards.

## Advantages
- Fine-grained, hierarchical resource accounting and enforcement.
- Enables safe multi-tenancy and higher bin-packing density per host.
- Delegatable: a parent group can sub-divide its budget among children.

## Disadvantages
- v1/v2 fragmentation complicates tooling and migration.
- Throttling artifacts (latency spikes) are not obvious from basic CPU%
  monitoring alone.

## Trade-offs
Strict hard limits give predictability but risk wasted capacity or OOM
kills at the ceiling; overcommitting resources improves density but risks
noisy-neighbor contention if usage spikes together.

## Alternatives
- `nice`/`ionice` — soft priority hints, no hard isolation boundary.
- Hypervisor-based VM partitioning — stronger isolation, higher overhead,
  coarser granularity.

## Performance Analysis
CPU quotas enforced over a short period can throttle a bursty process even
when its *average* usage is low, because the kernel resets the allowed
quota every period and blocks the group once it is spent; `cpu.stat`'s
`nr_throttled`/`throttled_usec` fields make this visible.

## Complexity Analysis (if applicable)
Not a traditional algorithm; accounting cost scales with the depth of the
cgroup hierarchy, since usage typically aggregates up through parent
groups on every update.

## Implementation
```bash
sudo mkdir /sys/fs/cgroup/mygroup
echo $$ | sudo tee /sys/fs/cgroup/mygroup/cgroup.procs
echo "100000 100000" | sudo tee /sys/fs/cgroup/mygroup/cpu.max   # 1 full CPU
echo "536870912" | sudo tee /sys/fs/cgroup/mygroup/memory.max    # 512Mi
```
Equivalent Kubernetes container resources that translate to these cgroup
settings on the node:
```yaml
resources:
  requests: { cpu: "250m", memory: "256Mi" }
  limits:   { cpu: "1",    memory: "512Mi" }
```

## Common Mistakes
- Setting a CPU limit too low for a bursty workload, causing throttling
  that looks like "the app is just slow."
- Conflating Kubernetes requests (scheduling input) with limits (hard
  ceiling) — requests do not cap usage.
- Assuming a memory-limit OOM kill always targets the "obviously" largest
  process rather than whichever one crosses the group's ceiling.

## Common Interview Questions

Q: What are cgroups, and what do they actually enforce?

Answer:
cgroups group processes and enforce/account for their use of CPU, memory,
I/O, and PIDs — answering "how much of this may this group consume,"
independent of what the group can see, which is the namespaces' job.

Q: What is the key structural difference between cgroups v1 and v2?

Answer:
v1 gives each controller its own separate hierarchy, so a process can
belong to different groups per resource; v2 unifies everything into one
hierarchy per group with controllers enabled per subtree, and is now the
default.

Q: What happens when a container exceeds its memory limit?

Answer:
The kernel first tries to reclaim memory from the cgroup; if that is not
enough, the cgroup-scoped OOM killer terminates a process inside that
group specifically, rather than affecting the rest of the host.

## Follow-up Interview Questions

Q: What is the difference between Kubernetes requests and limits from the cgroup's perspective?

Answer:
Requests only inform scheduling and never become an enforced cgroup
ceiling. Limits do (`memory.max`, `cpu.max`), so a pod can be scheduled on
its request and still be OOM-killed or throttled based on its limit.

## How the Interviewer Thinks
This checks whether the candidate understands what actually enforces
"container resource limits" mechanically, rather than repeating "cgroups"
without connecting it to memory reclaim or CPU quota behavior.

## Production Engineering Perspective
Set requests close to steady-state usage and limits with headroom for
bursts; watch throttling metrics, not just CPU%; Kubernetes QoS classes
(Guaranteed, Burstable, BestEffort) derive directly from requests/limits.

## Common Misconceptions
- "Containers are lightweight VMs with equally strong isolation" — cgroups
  bound usage but do not hide the kernel like a hypervisor boundary does.
- "A CPU limit guarantees performance" — a limit only caps usage; it never
  guarantees capacity is actually free when needed.
- "No configured limit means unlimited and therefore safe" — an
  unconstrained process can still starve every neighbor on the host.

## Revision Notes
- cgroups = enforcement of *how much*; namespaces = control of *what is
  visible* (next chapter).
- v2 unifies controllers into a single hierarchy; v1 splits them.
- CPU throttling is period-based and can bite low-average, bursty
  workloads.
- OOM kill under a memory cgroup is scoped to that group, not the host.

## Summary
cgroups give the kernel a real enforcement point for CPU, memory, and I/O
limits per group of processes, which is exactly what backs Kubernetes
requests/limits and systemd resource directives. Namespaces, covered next,
add the complementary half of container isolation: controlling what a
process can *see*.

---

# Namespaces

## What is it?
A namespace is a kernel feature that partitions a global system resource —
process IDs, network interfaces, mounts, hostname, IPC objects, user/group
IDs — so a process inside the namespace sees its own isolated instance of
that resource.

## Why do we need it?
Many independent workloads on one shared kernel each need their own view
of what processes exist, what the network looks like, and what is
mounted — without the cost of a separate VM per workload.

## What problem does it solve?
cgroups bound *how much* a group can use; namespaces solve the separate
problem of *what a process can see and name* — its own PID 1, its own
network stack, its own root filesystem — the visibility boundary that
makes something feel like an independent container.

## History (only if useful)
The mount namespace arrived first (2.4.19, 2002); UTS, IPC, PID, and
network namespaces followed through the 2.6 series. User namespaces,
remapping UIDs so "root" inside is unprivileged outside, matured around
2013-2016 and enabled rootless containers.

## How does it work?
`clone()` and `unshare()` accept flags such as `CLONE_NEWPID` or
`CLONE_NEWNET` that create a fresh namespace instance for the new (or
calling) process; `setns()` lets an existing process join a namespace that
already exists, which is how tools attach into a running container.

## Internal Working
- **PID namespace** — a separate process-ID tree; the first process
  inside becomes PID 1, with no visibility outside it.
- **Network namespace** — its own interfaces, routes, and iptables rules;
  connected to the host typically via a veth pair into a bridge.
- **Mount namespace** — its own view of what is mounted where, enabling
  `pivot_root` into a container's root filesystem.
- **UTS namespace** — its own hostname/domain name.
- **IPC namespace** — its own System V IPC objects and message queues.
- **User namespace** — remaps UIDs/GIDs, so UID 0 inside maps to an
  unprivileged UID outside.

## Step-by-Step Example
`docker run` roughly performs, via runc:
1. `clone()` with `CLONE_NEWPID|CLONE_NEWNET|CLONE_NEWNS|CLONE_NEWUTS|...`
   to start the container's init process inside fresh namespaces.
2. The new process becomes PID 1 inside its own PID namespace.
3. A veth pair is created; one end moves into the new network namespace,
   the other attaches to a bridge on the host.
4. `pivot_root` switches the mount namespace's root to the image's
   extracted filesystem.

## Visualization
```text
Host (root namespaces)                Container (child namespaces)
+-----------------------------+       +---------------------------+
| PID ns: real PID 4821       |       | PID ns: sees itself as    |
| Net ns: eth0, full route    |------>|         PID 1              |
|   \_ veth-host <--bridge--> |  veth | Net ns: eth0 (=veth-peer)  |
| Mount ns: full host tree    |  pair | Mount ns: image rootfs only|
+-----------------------------+       +---------------------------+
```

## Real-World Usage
- Every container runtime (Docker, containerd, CRI-O) uses namespaces as
  its core isolation primitive.
- A Kubernetes Pod's containers share the network and IPC namespaces (so
  they can reach each other on `localhost`) but typically keep separate
  mount namespaces.
- `ip netns` for manual network sandboxes; `unshare` for ad hoc isolation
  without any container tooling at all.

## Advantages
- Lightweight isolation with no separate kernel to boot, so startup is
  near-instant and overhead is minimal.
- Granular: a process can share some namespaces (e.g., network) while
  keeping others isolated (e.g., PID).

## Disadvantages
- A shared kernel means a kernel vulnerability can cross the isolation
  boundary — weaker than a hypervisor boundary.
- Not every resource is namespaced (time only gained partial namespace
  support around kernel 5.6), which can leak information between
  containers.

## Trade-offs
Sharing more namespaces (as Pod containers do for network/IPC) trades
isolation for easy local coordination; sharing fewer namespaces trades
coordination convenience for stronger separation.

## Alternatives
- Full hardware virtualization (KVM, Xen) for a genuinely separate kernel.
- gVisor or Kata Containers/Firecracker microVMs for a middle ground:
  container-like UX with a stronger sandbox boundary than plain namespaces.

## Performance Analysis
Namespace creation is fast (single-digit milliseconds) and adds negligible
steady-state overhead versus a bare process; a network namespace adds a
small, usually immaterial, per-packet cost from the extra veth/bridge hop.

## Complexity Analysis (if applicable)
Not algorithmic; namespace creation is an O(1) kernel operation per
requested flag, since it only allocates a fresh instance of the relevant
kernel data structure.

## Implementation
```bash
sudo unshare --pid --net --mount --fork --mount-proc bash
ps aux        # shows only this shell and its children
ip a          # shows no interfaces except loopback
```

## Common Mistakes
- Assuming namespace isolation is equivalent to a VM security boundary.
- Forgetting that a container's PID 1 must reap its own zombies (using an
  init like `tini`/`dumb-init`), since the host's PID 1 cannot see inside a
  separate PID namespace.
- Running containers `--privileged`, which disables much of the protection
  namespaces would otherwise provide.

## Common Interview Questions

Q: What is a namespace, and which resources can be namespaced on Linux?

Answer:
A namespace partitions a global kernel resource so a process gets its own
isolated view; the main ones are PID, network, mount, UTS (hostname), IPC,
user, and cgroup namespaces.

Q: What is the fundamental difference between what cgroups and namespaces each control?

Answer:
Namespaces control what a process can *see* — its own process tree,
network stack, or filesystem view; cgroups control how much of a resource
a process can *use*. Containers rely on both together for isolation.

Q: How does a container get its own network connectivity?

Answer:
Typically a veth (virtual Ethernet) pair: one end sits inside the
container's network namespace as `eth0`, the other stays on the host
attached to a bridge, connecting the isolated namespace to the network.

## Follow-up Interview Questions

Q: Why do all containers in the same Kubernetes Pod share the network namespace?

Answer:
So they can communicate over `localhost` and share a single IP and port
space, modeling "containers that belong together," while typically
keeping separate mount (and often PID) namespaces.

Q: What is a user namespace, and why does it enable rootless containers?

Answer:
It remaps UIDs/GIDs between namespace and host, so a process can be UID 0
("root") inside while running as an unprivileged UID on the host —
shrinking the blast radius if the container is compromised.

## How the Interviewer Thinks
Clearly separating "namespaces control visibility" from "cgroups control
usage" is a bar-raising signal — many candidates use the terms
interchangeably without knowing what each actually does.

## Production Engineering Perspective
Prefer rootless containers and user namespaces to shrink the blast radius
of a container escape; run an init as PID 1 to reap zombies correctly;
treat namespace isolation as one layer, not the whole security model.

## Common Misconceptions
- "cgroups and namespaces are the same mechanism" — they solve different
  problems (usage limits vs. visibility) and are configured separately.
- "Containers are kernel-isolated like virtual machines" — they share one
  kernel, so the attack surface is larger than a hypervisor boundary.
- "Every resource on Linux is namespaced" — some, like system time, only
  gained namespace support in recent kernel versions.

## Revision Notes
- Namespaces = visibility isolation; cgroups = usage limits.
- Pod containers commonly share network + IPC namespaces, not PID.
- User namespaces enable rootless containers by remapping UIDs.
- A container needs its own zombie-reaping init as PID 1.

## Summary
Namespaces give a process its own view of PIDs, network, mounts, hostname,
and IPC, while cgroups (previous chapter) bound its resource usage.
Together they are the two kernel primitives every container runtime is
built on — exactly what the next four chapters (Docker, containerd,
Kubernetes, Container Runtime Concepts) assemble into a usable platform.

---

# Docker

## What is it?
Docker is a platform for building, distributing, and running containers: it
packages an application and its dependencies into a portable image and
runs that image as an isolated process using namespaces and cgroups.

## Why do we need it?
Teams need software to behave identically on a laptop, in CI, and in
production without shipping a full virtual machine per service.

## What problem does it solve?
It solves environment drift ("works on my machine") by standardizing how
an application and its exact dependencies are packaged, distributed, and
started, on any machine that runs Docker.

## History (only if useful)
Docker (2013, dotCloud) initially wrapped LXC, then its own `libcontainer`.
Its popularity pushed the industry to standardize the container format
instead of depending on one vendor, leading to the OCI (2015) and later
the extraction of containerd and runc as independent projects.

## How does it work?
A `Dockerfile` builds a layered image; the image is pushed to and pulled
from a registry by content digest. `docker run` asks the Docker daemon to
start a container from that image, which delegates to containerd and an
OCI runtime (runc) under the hood.

## Internal Working
- Client (`docker` CLI) talks to `dockerd` over a REST API; `dockerd`
  delegates lifecycle to containerd over gRPC (Chapter 8).
- An image is a manifest plus read-only layers, addressed by digest;
  `overlay2` merges them into one copy-on-write filesystem at runtime.
- Networking defaults to a Linux bridge with iptables NAT, mapping
  container ports to host ports.

## Step-by-Step Example
```bash
docker build -t myapp:1.0 .
docker run -d -p 8080:80 --memory=256m myapp:1.0
```
`build` executes each instruction into a cached layer; `run` pulls if
needed, creates a container via containerd, wires a bridge interface, adds
an iptables DNAT rule mapping port 8080 to 80, and applies the memory
cgroup limit.

## Visualization
```text
docker CLI --REST--> dockerd --gRPC--> containerd --> shim --> runc --> kernel

image layers (read-only, cached):
  [base OS] -> [runtime deps] -> [app code] -> (writable container layer)
```

## Real-World Usage
- Multi-stage builds to keep production images small.
- `docker-compose` for local multi-container development.
- CI pipelines pushing images to registries (Hub, ECR, GHCR).

## Advantages
- Reproducible, fast-starting builds compared with full VMs.
- Huge ecosystem of registries, base images, and CI tooling.
- Layer caching speeds up both builds and repeated pulls.

## Disadvantages
- Careless Dockerfiles produce bloated, slow-to-pull images.
- A root daemon and privileged mode expand the attack surface.
- Storage-driver quirks (overlay2 copy-up) surprise newcomers.

## Trade-offs
Docker's all-in-one CLI/daemon UX is convenient but monolithic — exactly
why containerd and runc were split out as independent, reusable projects.

## Alternatives
- Podman — daemonless, rootless-by-default, CLI-compatible.
- Buildah/BuildKit — build-focused tools without a full runtime daemon.
- nerdctl — a Docker-like CLI built directly on containerd.

## Performance Analysis
Cold pulls are dominated by layer size over the network; a cached image
starts a container in well under a second since only a process, not a
kernel, must start. Large first writes to inherited files cost extra due
to overlay2 copy-up.

## Complexity Analysis (if applicable)
Not algorithmic; build time scales with the size of uncached layers, and
content-addressed layers let unrelated images share storage.

## Implementation
```dockerfile
FROM golang:1.22 AS build
WORKDIR /src
COPY . .
RUN go build -o app .

FROM gcr.io/distroless/base-debian12
COPY --from=build /src/app /app
USER 1000
ENTRYPOINT ["/app"]
```
```yaml
services:
  web:
    build: .
    ports: ["8080:80"]
    environment: [LOG_LEVEL=info]
```

## Common Mistakes
- Skipping multi-stage builds, shipping build tools into production.
- Running the application as root inside the container.
- Baking secrets into image layers, where they persist across later layers.

## Common Interview Questions

Q: What is the difference between a Docker image and a container?

Answer:
An image is an inert, versioned set of filesystem layers and metadata; a
container is a running process created from that image, with its own
writable layer, namespaces, and cgroup.

Q: How does Docker's layer caching work during a build?

Answer:
Each instruction produces a layer keyed by the instruction and its inputs;
if an earlier layer is unchanged, Docker reuses it and rebuilds only from
the first changed instruction onward — so rarely-changing steps should
come before frequently-changing ones.

Q: What actually happens when you run `docker run`?

Answer:
The CLI calls the daemon's API, which asks containerd to create a
container: containerd unpacks the image, builds an OCI bundle, and a shim
execs `runc` to create namespaces/cgroups and start the entrypoint.

## Follow-up Interview Questions

Q: Why did Docker split into containerd and runc instead of one engine?

Answer:
To give orchestrators a stable, minimal, vendor-neutral runtime layer, and
to let the low-level and high-level concerns evolve independently — the
split that led directly to OCI and CRI.

## How the Interviewer Thinks
This checks whether the candidate sees Docker as orchestrating other
components (build, containerd, runc, networking) rather than one opaque
box — the framing the next two chapters build on.

## Production Engineering Perspective
Scan images for vulnerabilities in CI; prefer minimal, pinned base images;
run as a non-root user; mirror registries to avoid pull-rate limits; use
rootless Docker or Podman where the threat model warrants it.

## Common Misconceptions
- "Containers are lightweight VMs" — they share the host kernel.
- "Docker is required to run containers" — containerd, runc, and Podman
  run standard OCI containers without it.
- "`docker build` always caches correctly" — unpinned tags and build
  context ordering can silently invalidate the cache.

## Revision Notes
- Image = static layers; container = running process from an image.
- `docker run` path: CLI → dockerd → containerd → shim → runc.
- Multi-stage builds and minimal bases keep images small and secure.

## Summary
Docker popularized a portable image format and simple UX, but delegates
the actual container lifecycle to containerd and runc — covered next —
which is what Kubernetes and every other orchestrator depend on.

---

# containerd

## What is it?
containerd is an industry-standard, high-level container runtime daemon
that manages the container lifecycle — image transfer, storage, and
supervision — behind a stable gRPC API.

## Why do we need it?
Docker, Kubernetes, and other platforms all need the same low-level
container lifecycle logic; without a shared component, each would
duplicate, and subtly diverge in, image handling and supervision.

## What problem does it solve?
It provides one minimal, embeddable runtime layer so higher-level tools
(the Docker CLI, Kubernetes' kubelet) do not each need their own image
store, snapshot manager, and process supervisor.

## History (only if useful)
containerd was extracted from Docker Engine in 2016, donated to the CNCF
in 2017, and graduated in 2019. Kubernetes removed its Docker-specific
dockershim in version 1.24 (2022), making containerd (or CRI-O) the
direct, default runtime path.

## How does it work?
containerd exposes a gRPC API used by `crictl`/`ctr` or a kubelet's CRI
plugin. It manages image pulls and on-disk snapshots itself, then starts
one `containerd-shim` per container, which execs the low-level OCI runtime
(typically `runc`).

## Internal Working
- **Content store** holds image layer blobs by digest; **snapshotters**
  (overlayfs) turn layers into a copy-on-write root filesystem.
- **Shim** stays alive independently of containerd, so restarting or
  upgrading the daemon does not kill running containers.
- **CRI plugin** translates Kubernetes' CRI calls into containerd's
  native APIs; containerd's own metadata namespaces (e.g. `k8s.io`) isolate
  clients and are unrelated to kernel namespaces (Chapter 6).

## Step-by-Step Example
kubelet asks its CRI plugin to run a pod: `RunPodSandbox` creates the
shared sandbox owning the pod's network namespace; `CreateContainer` and
`StartContainer` unpack each app image, build an OCI bundle, and a shim
execs `runc`, then reports the running PID back to containerd.

## Visualization
```text
kubelet --CRI(gRPC)--> containerd --> shim (per container) --> runc --> kernel
                              |
                              +--> content store + snapshotter (images/layers)
```

## Real-World Usage
- Default runtime in most managed Kubernetes (EKS, GKE, AKS).
- `crictl ps`/`ctr images pull` for node-level debugging without Docker.
- `RuntimeClass` selects an alternate low-level runtime (Kata, gVisor).

## Advantages
- Minimal, stable API surface shared safely across platforms.
- Shim architecture allows daemon restarts without killing containers.
- Pluggable snapshotters and runtimes support diverse needs.

## Disadvantages
- No built-in build feature or compose-style multi-container UX.
- Requires learning `crictl`/`ctr` instead of the familiar `docker` CLI.

## Trade-offs
containerd deliberately does less than Docker — no build, no compose — for
a smaller, more stable surface that many orchestrators can depend on at
once.

## Alternatives
- CRI-O — a minimal runtime built specifically for Kubernetes' CRI.
- Docker Engine via the now-removed dockershim — no longer used by k8s.

## Performance Analysis
The shim-per-container model adds small, fixed memory overhead in exchange
for resilience; gRPC overhead is negligible next to process-exec time;
content-addressed layers avoid redundant downloads/storage across images.

## Complexity Analysis (if applicable)
Not algorithmic; image unpack time scales with total layer size, and
deduplication via shared layers avoids redundant work across images.

## Implementation
```bash
crictl pods
crictl ps -a
crictl logs <container-id>
ctr images pull docker.io/library/nginx:alpine
```
```toml
[plugins."io.containerd.grpc.v1.cri".containerd.runtimes.kata]
  runtime_type = "io.containerd.kata.v2"
```

## Common Mistakes
- Expecting `docker ps` to show containers when only containerd is
  installed.
- Confusing containerd's metadata namespaces with kernel namespaces.
- Running `crictl` without configuring its runtime endpoint.

## Common Interview Questions

Q: How does containerd relate to Docker and to Kubernetes?

Answer:
containerd was extracted from Docker Engine to be a standalone, reusable
runtime; Docker now uses it internally, and Kubernetes' kubelet talks to
it (or CRI-O) directly via CRI, with no Docker Engine involved.

Q: What is a shim, and why does containerd use one per container?

Answer:
A shim execs the low-level OCI runtime and stays behind as the
container's direct parent; this decouples a container's lifetime from
containerd's own, so containerd can restart without killing workloads.

Q: Why was dockershim removed from Kubernetes?

Answer:
It was Docker-specific translation code Kubernetes had to maintain just to
keep talking to Docker Engine; once CRI became standard, talking directly
to a CRI-native runtime removed that maintenance burden and an extra hop.

## Follow-up Interview Questions

Q: How would you inspect containers on a node with no Docker installed?

Answer:
Use `crictl ps`/`pods`/`logs`, or the lower-level `ctr`, both of which
talk directly to containerd's gRPC API without any Docker Engine present.

Q: What happens to running containers if containerd itself crashes?

Answer:
They keep running, because each container's actual parent process is its
shim, not containerd; on restart, containerd reconnects to the existing
shims and resumes managing the already-running containers.

## How the Interviewer Thinks
This separates candidates who can operate at the node level (crictl,
runtime classes, shim behavior) from those who only know `kubectl` — an
important signal for platform and SRE roles.

## Production Engineering Perspective
Use `crictl` for node-level debugging when Docker is absent; pick a
`RuntimeClass` for workloads needing stronger sandboxing; monitor
containerd's own logs/metrics separately from application logs.

## Common Misconceptions
- "containerd is just Docker's internal engine" — it is an independent
  CNCF project usable standalone.
- "Removing dockershim removed Docker support" — only Docker Engine as
  runtime was removed; images remain standard OCI artifacts.
- "containerd can build images" — building is a separate concern (e.g.
  BuildKit).

## Revision Notes
- containerd = image lifecycle + storage + supervision behind gRPC.
- One shim per container decouples container lifetime from the daemon's.
- crictl/ctr are the node-level tools when Docker is not installed.

## Summary
containerd is the shared engine Docker and Kubernetes both build on to run
containers, using a shim per container for resilience, and speaking CRI to
Kubernetes — detailed further in Chapter 10.

---

# Kubernetes

## What is it?
Kubernetes is a control plane that keeps a fleet of nodes converging on a
desired state — declared as objects like Pods, Deployments, and Services —
by continuously reconciling reality toward that declaration.

## Why do we need it?
Running containers on one host (Chapters 7-8) does not answer: which of
many nodes should a container run on, what restarts it after a crash, how
do other services find it, and how does it survive a node dying.

## What problem does it solve?
It solves fleet-wide scheduling, self-healing, service discovery, rolling
updates, and scaling, so operators declare desired state instead of
scripting each of those behaviors by hand.

## History (only if useful)
Google open-sourced Kubernetes in 2014, distilling lessons from its
internal Borg/Omega schedulers. It became the CNCF's first graduated
project (2018) and the de facto standard orchestrator.

## How does it work?
Every object (Pod, Deployment, Service, ...) is stored in `etcd` via the
API server. Independent **controllers** watch for drift between desired
and actual state and act to correct it; the **scheduler** assigns Pods to
nodes; each node's **kubelet** and **kube-proxy** make that assignment
real.

## Internal Working
- **Control plane**: `kube-apiserver` (front door + validation), `etcd`
  (state store), `kube-scheduler` (Pod placement), `kube-controller-manager`
  (Deployment/ReplicaSet/Node controllers), `cloud-controller-manager`.
- **Node**: `kubelet` (talks to the container runtime via CRI, Chapter 10,
  and runs probes), `kube-proxy` (programs Service iptables/IPVS rules),
  the container runtime itself.
- **Reconciliation loop**: `desired state (spec) - observed state (status)
  -> action`, repeated forever; this is why Kubernetes is self-healing.
- A **Pod** is the smallest deployable unit: one or more containers that
  share a network namespace and, optionally, storage — via a paused
  "pause" container that owns the sandbox (Chapter 6/8).

## Step-by-Step Example
```bash
kubectl apply -f deployment.yaml
```
1. API server validates and persists the Deployment to etcd.
2. The Deployment controller creates a ReplicaSet; the ReplicaSet
   controller creates Pod objects to match `replicas`.
3. The scheduler assigns each unscheduled Pod to a node using filtering
   (fits resources/taints) then scoring (spreads load).
4. That node's kubelet asks the container runtime to start the Pod's
   sandbox and containers, and reports status back to the API server.

## Visualization
```text
          +----------------- control plane -----------------+
          |  API server <-> etcd                             |
          |     ^   \                                        |
          |     |    +--> scheduler (assigns Pod -> Node)     |
          |     +--> controller-manager (Deployment/RS/Node)  |
          +---------------------|-----------------------------+
                                 | watch/update
                    +------------+------------+
                    |           node           |
                    |  kubelet --CRI--> runtime |
                    |  kube-proxy (Service IPs) |
                    +---------------------------+
```

## Real-World Usage
- Deployments for stateless web/API services with rolling updates.
- StatefulSets for databases/queues needing stable identity and storage.
- DaemonSets for per-node agents (log shippers, CNI, monitoring).

## Advantages
- Declarative, self-healing: crashed Pods and failed nodes are replaced
  automatically.
- Portable across clouds and on-prem via a consistent API.
- Rich ecosystem: Helm, operators, service meshes, GitOps tooling.

## Disadvantages
- Substantial operational complexity and a steep learning curve.
- Many moving parts (etcd, controllers, CNI, CSI) to secure and upgrade.

## Trade-offs
Kubernetes trades simplicity for generality and resilience: a single VM
with a supervisor is simpler for one small service, but does not scale
operationally across dozens of services and hundreds of nodes.

## Alternatives
- Nomad — a simpler, multi-workload-type scheduler.
- Docker Swarm — simpler orchestration, far smaller ecosystem.
- Managed serverless containers (Cloud Run, Fargate) — no node/cluster
  management, less control over scheduling and networking.

## Performance Analysis
The scheduler filters and scores nodes near-instantly at moderate scale;
etcd write latency and API server request volume are the usual scaling
limits on very large clusters. Pod start latency is dominated by image
pull time, not Kubernetes' own overhead.

## Complexity Analysis (if applicable)
Scheduling one Pod is roughly O(nodes) to filter and score; watch-based
reconciliation (versus polling) keeps controller work proportional to the
rate of change, not cluster size.

## Implementation
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: web
spec:
  replicas: 3
  selector:
    matchLabels: {app: web}
  template:
    metadata:
      labels: {app: web}
    spec:
      containers:
        - name: web
          image: myapp:1.0
          resources:
            requests: {cpu: "100m", memory: "128Mi"}
            limits: {memory: "256Mi"}
          readinessProbe:
            httpGet: {path: /healthz, port: 8080}
---
apiVersion: v1
kind: Service
metadata:
  name: web
spec:
  selector: {app: web}
  ports: [{port: 80, targetPort: 8080}]
```

## Common Mistakes
- Omitting resource requests/limits, starving the scheduler of the data it
  needs and risking noisy-neighbor CPU/memory contention.
- No readiness probe, so traffic reaches Pods before they're ready.
- Ignoring `PodDisruptionBudget`s, letting maintenance take a service down.

## Common Interview Questions

Q: What is a Pod, and why not just schedule containers directly?

Answer:
A Pod is one or more containers sharing a network namespace (and
optionally storage), scheduled as one atomic unit; this models
tightly-coupled processes — e.g. an app and a log-shipper sidecar — that
must co-locate, which scheduling bare containers cannot guarantee.

Q: How does a Service route traffic to the right Pods?

Answer:
A Service selects Pods by label selector; kube-proxy watches the
resulting Endpoints and programs iptables/IPVS rules on every node, so
traffic to the Service's virtual IP load-balances to a matching Pod.

Q: What happens when a node fails?

Answer:
The node controller marks it `NotReady` after missed heartbeats; after a
grace period it evicts the node's Pods, and their owning
ReplicaSet/StatefulSet controllers create replacements, which the
scheduler places on healthy nodes.

## Follow-up Interview Questions

Q: How does the scheduler decide which node gets a Pod?

Answer:
It filters out nodes that cannot fit the Pod (insufficient resources,
unmatched taints/node-selectors/affinity), then scores the remainder
(e.g. resource balance, spreading across zones) and picks the
highest-scoring node.

Q: A Pod is stuck in CrashLoopBackOff — how do you debug it?

Answer:
`kubectl describe pod` for events and exit code, `kubectl logs
--previous` for the last crash's output; a nonzero exit or `OOMKilled`
reason often points to a misconfigured command, missing dependency, or a
memory limit set too low.

## How the Interviewer Thinks
This tests whether a candidate sees Kubernetes as cooperating control
loops rather than a monolith, and can reason about failure modes (node
loss, crash loops, scheduling) instead of only `kubectl` syntax.

## Production Engineering Perspective
Set resource requests/limits and probes on every workload; define
PodDisruptionBudgets for availability during upgrades; use namespaces and
RBAC for tenant isolation; watch API server/etcd latency as leading
indicators of cluster-wide degradation; prefer `RuntimeClass`-selected
sandboxing for untrusted workloads (Chapter 6/10).

## Common Misconceptions
- "A container and a Pod are the same thing" — a Pod can hold several
  co-scheduled containers.
- "Kubernetes runs containers itself" — the kubelet delegates to a CRI
  runtime (Chapter 10); Kubernetes only orchestrates.
- "A Service is a load balancer process" — it is a stable virtual IP and a
  set of iptables/IPVS rules, not a running proxy per request.

## Revision Notes
- Pod = smallest schedulable unit; shares network namespace across its
  containers.
- Controllers continuously reconcile spec vs. status; this is the
  self-healing mechanism.
- Scheduler = filter then score; Service = label selector + kube-proxy
  rules, not a proxy process.

## Summary
Kubernetes orchestrates *where* and *how many* containers run and keeps
that true over time, but still depends on a CRI-compliant runtime on each
node to actually start containers — the OCI/CRI boundary covered next.

---

# Container Runtime Concepts

## What is it?
This chapter names the standard interfaces — OCI and CRI — and the
runtime "layers" (high-level vs. low-level) introduced piecemeal in
Chapters 7-9, and shows precisely where the image ends and the runtime
begins.

## Why do we need it?
Without shared standards, every orchestrator would need custom code per
container engine, and every engine would need a proprietary image format —
exactly the fragmentation the industry deliberately avoided after 2015.

## What problem does it solve?
OCI standardizes *what* an image and a running container are; CRI
standardizes *how* an orchestrator talks to whatever runtime is installed
— decoupling Kubernetes from any single vendor's implementation.

## History (only if useful)
Docker, CoreOS, and others formed the Open Container Initiative (OCI)
under the Linux Foundation in 2015 to prevent a runtime/image format
split. Kubernetes introduced CRI in 2016 and removed its Docker-specific
shim in 2022, completing the move to standard interfaces end-to-end.

## How does it work?
- **OCI Image Spec** defines a content-addressed image: a manifest
  pointing at gzipped filesystem layer tarballs plus JSON config
  (entrypoint, env, etc.).
- **OCI Runtime Spec** defines an on-disk "bundle" (root filesystem +
  `config.json`) and the verbs (`create`, `start`, `kill`, `delete`) any
  compliant low-level runtime must implement — `runc` is the reference
  implementation.
- **CRI** (Container Runtime Interface) is a gRPC API kubelet calls; any
  runtime that implements it (containerd, CRI-O) can run Kubernetes Pods.

## Internal Working
```text
   High-level runtime           Low-level (OCI) runtime
   (containerd, CRI-O)   -->    (runc, crun, Kata, gVisor)
   image mgmt, storage,         namespaces + cgroups + exec,
   CRI server, shims            per the OCI Runtime Spec
```
- **Image boundary**: the OCI Image Spec ends once layers are unpacked
  into a root filesystem on disk — a static artifact, not yet a process.
- **Runtime boundary**: the OCI Runtime Spec begins there — turning a root
  filesystem plus `config.json` into a running, isolated process.
- CRI sits one level above both: it lets kubelet say "run this Pod" without
  knowing which high- or low-level runtime is actually installed.

## Step-by-Step Example
1. kubelet's CRI plugin calls containerd's `CreateContainer`.
2. containerd's snapshotter unpacks OCI image layers into a root
   filesystem (the image boundary is crossed here).
3. containerd writes an OCI bundle (`config.json` + rootfs) and its shim
   execs `runc create` then `runc start` (the runtime boundary).
4. `runc` builds namespaces/cgroups (Chapters 5-6) and `execve`s the
   entrypoint (Chapter 3); `runc` then exits, leaving the shim as parent.

## Visualization
```text
   CRI (gRPC)                 OCI Image Spec        OCI Runtime Spec
kubelet ---------> containerd ------------> rootfs ---------------> runc -> process
          "what to run"        "what an image is"   "how to run a bundle"
```

## Real-World Usage
- `RuntimeClass` in Kubernetes selects `runc` for trusted workloads or
  Kata/gVisor for stronger isolation of untrusted/multi-tenant code.
- Registries (Docker Hub, ECR, GHCR, Quay) distribute OCI images
  interchangeably across any OCI-compliant client.
- `skopeo`/`buildah`/BuildKit build and inspect OCI images without needing
  a full container engine.

## Advantages
- Any CRI runtime works with any Kubernetes distribution — no vendor
  lock-in at the node level.
- Any OCI image runs on any OCI runtime — Docker-built images run
  unmodified under Podman, containerd, or CRI-O.
- Alternate low-level runtimes (Kata's microVMs, gVisor's user-space
  kernel) drop in without any change to Kubernetes or images.

## Disadvantages
- Multiple layers (CRI, containerd, shim, OCI runtime) add indirection
  that complicates debugging and tracing a request end-to-end.
- Spec compliance still leaves runtime-specific behavior (performance,
  supported features) that operators must understand.

## Trade-offs
Standardizing at the image and runtime-invocation level (not the entire
engine) preserves room for innovation above and below — different
snapshotters, isolation technologies, and CRI implementations — while
guaranteeing interoperability at the boundary that matters most.

## Alternatives
- Proprietary, non-OCI formats/runtimes (largely obsolete: pre-OCI Docker,
  old AppC/rkt) — abandoned in favor of the shared standard.
- gVisor/Kata as alternative low-level runtimes for stronger, VM- or
  syscall-filter-based isolation than `runc`'s namespaces/cgroups alone.

## Performance Analysis
`runc` adds negligible overhead (a short-lived setup process); Kata/gVisor
trade some startup latency and per-syscall overhead for materially
stronger isolation — a security/performance trade-off chosen per
workload's trust level via `RuntimeClass`.

## Complexity Analysis (if applicable)
Not algorithmic; each additional layer (CRI, shim, low-level runtime) adds
a roughly constant number of hops per container start, not a scaling
concern at the sizes Kubernetes nodes typically run.

## Implementation
```yaml
apiVersion: node.k8s.io/v1
kind: RuntimeClass
metadata:
  name: kata
handler: kata
---
apiVersion: v1
kind: Pod
metadata: {name: untrusted}
spec:
  runtimeClassName: kata
  containers: [{name: app, image: myapp:1.0}]
```
```bash
runc spec               # generate a starter config.json
runc create mycontainer # build namespaces/cgroups from a bundle
runc start mycontainer  # exec the entrypoint
```

## Common Mistakes
- Assuming "container runtime" means one specific thing, rather than
  distinguishing high-level (containerd/CRI-O) from low-level (runc/Kata).
- Believing CRI is a container format — it is an API contract, not a spec
  for images or bundles.
- Assuming every workload needs the same isolation level, instead of
  choosing a `RuntimeClass` per trust boundary.

## Common Interview Questions

Q: What is the difference between OCI and CRI?

Answer:
OCI standardizes the image format and the low-level runtime's bundle/verbs
(what a container *is* and how it starts); CRI standardizes the gRPC API
an orchestrator uses to ask a high-level runtime to manage Pods — a
different layer entirely.

Q: Where exactly is the boundary between an image and a running container?

Answer:
The image ends once its layers are unpacked to a root filesystem on disk
(a static artifact); the runtime boundary begins when that rootfs plus
`config.json` is handed to `runc`, which builds namespaces/cgroups and
execs the entrypoint.

Q: Why does Kubernetes not talk to `runc` directly?

Answer:
`runc` only starts a single bundle; it does nothing for image pulls,
storage, or Pod-level sandbox networking. containerd/CRI-O provide those
via CRI, then delegate the final process-start step to `runc`.

## Follow-up Interview Questions

Q: When would you choose Kata Containers or gVisor over runc?

Answer:
For multi-tenant or untrusted workloads where namespace/cgroup isolation
alone is insufficient — Kata runs each container in a lightweight VM, and
gVisor intercepts syscalls in a user-space kernel, trading some
performance for a smaller attack surface.

Q: Can an image built with Docker run under containerd/CRI-O directly?

Answer:
Yes — Docker produces standard OCI images, and any OCI-compliant runtime
can pull and run them without Docker Engine present, which is exactly why
Kubernetes could remove dockershim without breaking existing images.

## How the Interviewer Thinks
This capstone set reveals whether a candidate can precisely place OCI,
CRI, containerd, and runc relative to each other, rather than treating
"the container runtime" as one black box.

## Production Engineering Perspective
Choose `RuntimeClass` per workload trust level; keep images and runtimes
both OCI-compliant to preserve portability across clusters/clouds; when
debugging, identify which layer (CRI call, image unpack, bundle, `runc`
exec) a failure occurred at instead of guessing at "the container" broadly.

## Common Misconceptions
- "Docker, containerd, and runc are three names for the same thing" —
  they are three distinct layers with different responsibilities.
- "CRI is part of the OCI spec" — they are separate standards from
  different origins (Kubernetes vs. the Linux Foundation) solving
  different problems.
- "An OCI image is already a container" — it becomes one only once
  unpacked into a bundle and started by a runtime.

## Revision Notes
- OCI Image Spec = what an image is; OCI Runtime Spec = how a bundle
  becomes a process; CRI = how an orchestrator talks to a runtime.
- Layer order: kubelet -> CRI -> containerd/CRI-O -> shim -> runc/Kata/gVisor
  -> kernel namespaces + cgroups.
- Image boundary = layers unpacked to rootfs; runtime boundary = bundle
  handed to the low-level runtime.

## Summary
OCI and CRI are why Chapters 7-9's tools interoperate at all: any OCI
image runs on any OCI runtime, and any CRI-compliant runtime plugs into
Kubernetes — closing the loop from Chapter 3's `fork`/`exec` up to a
`Deployment` rolling out Pods across a cluster.
