use serde::Deserialize;

#[derive(Debug, Deserialize)]
pub enum ShellCommands {
    // Restart,
    PkgInstall,
    PkgRemove,
}
