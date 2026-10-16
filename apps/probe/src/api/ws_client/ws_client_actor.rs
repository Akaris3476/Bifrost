use std::sync::Arc;
use async_trait::async_trait;
use ezsockets::{Client};
use crate::api::handlers::run_sh_command::run_command;
use crate::api::api_service::AppState;
use crate::api::contracts::message::IncomingCommand;

pub(crate) struct WsClientActor {
    tx_to_ws: Client<Self>,
    app_state: Arc<AppState>
    
}

impl WsClientActor {
    pub fn new(tx_to_ws: Client<Self>, app_state: Arc<AppState>) -> Self { 
         Self { tx_to_ws, app_state }
    }
}

#[async_trait]
impl ezsockets::ClientExt for WsClientActor {
    
    type Call = ();

    async fn on_text(&mut self, text: ezsockets::Utf8Bytes) -> Result<(), ezsockets::Error> {
        println!("received message: {text}");

        match serde_json::from_str::<IncomingCommand>(&text) {
            Ok(cmd) => {
                
                let res = self.incoming_cmd_handler(cmd).await;
            
                self.tx_to_ws.text(res)?;
                
            }
            Err(e) => {
                eprintln!("Error parsing JSON: {:?}", e);
            }
            
        }
        
        Ok(())
    }
    
    

    async fn on_binary(&mut self, bytes: ezsockets::Bytes) -> Result<(), ezsockets::Error> {
        println!("received bytes: {bytes:?}");
        Ok(())
    }

    async fn on_call(&mut self, call: Self::Call) -> Result<(), ezsockets::Error> {
        let () = call;
        Ok(())
    }
}

impl WsClientActor {
    async fn incoming_cmd_handler(&mut self, cmd: IncomingCommand) -> String {

        let res = match cmd {

            IncomingCommand::ShellExecute(run_sh_cmd_req) => {
                let res = run_command(self.app_state.clone(), run_sh_cmd_req).await;
                
                res
            }

        };
        
        let serialized_res = serde_json::to_string(&res)
            .unwrap_or("Error parsing response to JSON".to_string());


        serialized_res
    }
    
    
    
}


