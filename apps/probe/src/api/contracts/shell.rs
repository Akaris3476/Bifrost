use serde::{Deserialize, Serialize};
use crate::services::shell_service::shell_commands::ShellCommands;

#[derive(Deserialize)]
pub struct RunShCommandRequest {
    pub action_type: ShellCommands,
    pub args: Vec<String>
}

#[derive(Serialize)]
pub struct RunShCommandResponse {
    pub stdout: String,
    pub stderr: String,
    pub command_succeeded: bool,
    pub command_started: bool,
    pub exit_code: Option<i32>,
    pub status_code: String
}