use serde::{Deserialize, Serialize};
use crate::api::contracts::shell::{RunShCommandRequest, RunShCommandResponse};

#[derive(Deserialize)]
pub struct IncomingMessage {
    pub message_id: String, 

    #[serde(flatten)]
    pub command: IncomingCommand,
}

#[derive(Deserialize)]
#[serde(tag = "command", content = "payload", rename_all = "snake_case")]
pub enum  IncomingCommand {
    ShellExecute(RunShCommandRequest)
}

#[derive(Serialize)]
pub struct  OutgoingMessage {
     pub message_id: String,
    
    #[serde(flatten)]
    pub message: OutgoingResponse,

}

#[derive(Serialize)]
#[serde(tag = "command", content = "payload", rename_all = "snake_case")]
pub enum OutgoingResponse {
    ShellExecuteResponse(RunShCommandResponse),
}



