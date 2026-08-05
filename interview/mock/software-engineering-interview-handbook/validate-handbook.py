#!/usr/bin/env python3
from pathlib import Path
import re
import sys


ROOT = Path(__file__).resolve().parent

PARTS = {
    "parts/part-01-data-structures-and-algorithms.md": [
        "Complexity Analysis", "Arrays", "Strings", "Hashing", "Two Pointers",
        "Sliding Window", "Prefix Sum", "Binary Search", "Linked Lists",
        "Stack", "Queue", "Heap", "Trees", "Binary Search Trees", "Trie",
        "Graphs", "Union Find", "Greedy Algorithms", "Backtracking",
        "Dynamic Programming", "Segment Tree", "Fenwick Tree",
        "Bit Manipulation",
    ],
    "parts/part-02-operating-systems.md": [
        "Processes", "Threads", "Scheduling", "Context Switching",
        "Virtual Memory", "Paging", "TLB", "Page Faults", "Mutex", "Semaphore",
        "Condition Variables", "Read-Write Locks", "Deadlocks", "fork", "exec",
        "mmap", "Kernel Space vs User Space",
    ],
    "parts/part-03-computer-networks.md": [
        "OSI Model", "TCP/IP", "TCP", "UDP", "Three-Way Handshake",
        "Connection Termination", "Flow Control", "Congestion Control", "DNS",
        "ARP", "NAT", "HTTP", "HTTPS", "TLS", "Reverse Proxy",
        "Load Balancer", "CDN", "WebSockets", "gRPC",
    ],
    "parts/part-04-databases.md": [
        "SQL", "Indexes", "B+ Trees", "Hash Indexes", "Transactions", "ACID",
        "MVCC", "Isolation Levels", "Locks", "Query Optimization",
        "Replication", "Sharding", "WAL", "UUIDs", "PostgreSQL Internals",
        "InnoDB Internals", "HOT Updates", "CTID",
    ],
    "parts/part-05-distributed-systems.md": [
        "CAP Theorem", "PACELC", "Replication", "Consistent Hashing",
        "Leader Election", "Consensus", "Raft", "Paxos", "Lamport Clock",
        "Vector Clock", "Kafka", "Redis", "ZooKeeper", "etcd", "Idempotency",
        "Retry Mechanisms", "Exponential Backoff", "Circuit Breakers",
        "Distributed Caching", "Service Discovery",
    ],
    "parts/part-06-linux-and-infrastructure.md": [
        "Linux Commands", "File System", "Process Management", "systemd",
        "cgroups", "Namespaces", "Docker", "containerd", "Kubernetes",
        "Container Runtime Concepts",
    ],
    "parts/part-07-java.md": [
        "JVM", "Garbage Collection", "Collections", "HashMap Internals",
        "ConcurrentHashMap", "ExecutorService", "CompletableFuture",
        "synchronized", "ReentrantLock", "Java Memory Model",
    ],
    "parts/part-08-system-design.md": [
        "Design Fundamentals", "Scalability", "Availability", "Consistency",
        "Caching", "Rate Limiter", "URL Shortener", "Chat System",
        "Notification System", "File Storage", "Logging", "Monitoring",
    ],
    "parts/part-09-behavioral-preparation.md": [
        "Tell Me About Yourself", "Resume Walkthrough", "Project Discussion",
        "Production Incidents", "Failure Stories", "Conflict Resolution",
        "Leadership", "Decision Making",
    ],
}

COMPANIONS = [
    "companions/last-minute-revision-guide.md",
    "companions/dsa-cheat-sheet.md",
    "companions/system-design-cheat-sheet.md",
    "companions/interview-qa-companion.md",
]

SECTIONS = [
    "What is it?", "Why do we need it?", "What problem does it solve?",
    "History (only if useful)", "How does it work?", "Internal Working",
    "Step-by-Step Example", "Visualization", "Real-World Usage", "Advantages",
    "Disadvantages", "Trade-offs", "Alternatives", "Performance Analysis",
    "Complexity Analysis (if applicable)", "Implementation", "Common Mistakes",
    "Common Interview Questions", "Follow-up Interview Questions",
    "How the Interviewer Thinks", "Production Engineering Perspective",
    "Common Misconceptions", "Revision Notes", "Summary",
]


def next_nonblank(lines: list[str], start: int) -> str:
    for line in lines[start:]:
        if line.strip():
            return line.strip()
    return ""


errors: list[str] = []
all_paths = [*PARTS, *COMPANIONS, "README.md", "front-matter.md"]

for relative in all_paths:
    if not (ROOT / relative).is_file():
        errors.append(f"Missing file: {relative}")

for relative, topics in PARTS.items():
    path = ROOT / relative
    if not path.is_file():
        continue
    text = path.read_text(encoding="utf-8")
    lines = text.splitlines()
    chapter_positions = {
        line[2:].strip(): index
        for index, line in enumerate(lines)
        if line.startswith("# ") and not line.startswith("## ")
    }

    for topic in topics:
        if topic not in chapter_positions:
            errors.append(f"{relative}: missing chapter '# {topic}'")
            continue
        start = chapter_positions[topic]
        end = next(
            (i for i in range(start + 1, len(lines)) if lines[i].startswith("# ")),
            len(lines),
        )
        chapter = "\n".join(lines[start:end])
        for section in SECTIONS:
            if f"## {section}" not in chapter:
                errors.append(f"{relative}: {topic} missing '## {section}'")

    for index, line in enumerate(lines):
        if re.match(r"^\s*Q:", line):
            if next_nonblank(lines, index + 1) != "Answer:":
                errors.append(
                    f"{relative}:{index + 1}: question lacks immediate Answer:"
                )

for relative in COMPANIONS:
    path = ROOT / relative
    if not path.is_file():
        continue
    lines = path.read_text(encoding="utf-8").splitlines()
    for index, line in enumerate(lines):
        if re.match(r"^\s*Q:", line):
            if next_nonblank(lines, index + 1) != "Answer:":
                errors.append(
                    f"{relative}:{index + 1}: question lacks immediate Answer:"
                )

handbook = ROOT / "HANDBOOK.md"
if handbook.is_file():
    words = len(re.findall(r"\b[\w'-]+\b", handbook.read_text(encoding="utf-8")))
    estimated_pages = words / 450
    print(f"Handbook words: {words:,}")
    print(f"Estimated dense Markdown pages: {estimated_pages:.0f}")

if errors:
    print("\n".join(f"ERROR: {error}" for error in errors))
    sys.exit(1)

print("Handbook validation passed.")
