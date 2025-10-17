curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh

source $HOME/.cargo/env
rustc --version
cargo --version

TO run a program

```
rustc main.rs
./main
```

cargo new rust_server
cd rust_server
cargo add actix-web
cargo add serde --features derive

cargo run

