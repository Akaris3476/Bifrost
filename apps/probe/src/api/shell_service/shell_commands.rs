use serde::Deserialize;

// use serde::Deserialize;
// 
// #[derive(Deserialize)]
#[derive(Debug, Deserialize)]
pub enum ShellCommands {
    Restart,
    Install
}
