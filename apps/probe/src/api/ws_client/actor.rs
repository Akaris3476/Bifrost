use std::sync::Arc;
use std::time::Duration;
use async_trait::async_trait;
use ezsockets::{Client, Error, WSError};
use ezsockets::client::ClientCloseMode;
use tokio::task::JoinHandle;
use crate::api::handlers::run_sh_command::run_command;
use crate::api::api_service::AppState;
use crate::api::contracts::message::{IncomingCommand, IncomingMessage, OutgoingMessage, OutgoingResponse};
use crate::services::sysinfo_service::SysMetrics;

pub(crate) struct WsClientActor {
    tx_to_ws: Client<Self>,
    app_state: Arc<AppState>,
    
    res_failed_to_send: Vec<String>,
    handle: Option<JoinHandle<()>>
}

impl WsClientActor {
    pub fn new(tx_to_ws: Client<Self>, app_state: Arc<AppState>) -> Self { 
        println!("Actor created successfully!");
         Self { tx_to_ws, app_state, res_failed_to_send: vec![], handle: None }
    }
}

#[async_trait]
impl ezsockets::ClientExt for WsClientActor {
    
    type Call = ();
    
    async fn on_text(&mut self, text: ezsockets::Utf8Bytes) -> Result<(), Error> {
        println!("received message: {text}");

        match serde_json::from_str::<IncomingMessage>(&text) {
            Ok(msg) => {
                
                let res = self.incoming_msg_handler(msg).await;
            
                if let Err(err ) = self.tx_to_ws.text(res.clone()) {
                    self.res_failed_to_send.push(res);
                    println!("Error sending message: {err}. It's been saved.");
                }
                
            }
            Err(e) => {
                eprintln!("Error parsing JSON: {:?}", e);
                let _ = self.tx_to_ws.text("Error parsing incoming message")
                    .inspect_err(|_| println!("Failed to inform server of incoming message parsing error"));
            }
            
        }
        
        Ok(())
    }
    
    

    async fn on_binary(&mut self, bytes: ezsockets::Bytes) -> Result<(), Error> {
        println!("received bytes: {bytes:?}");
        Ok(())
    }

    async fn on_call(&mut self, call: Self::Call) -> Result<(), Error> {
        let () = call;
        Ok(())
    }
    
    async fn on_connect(&mut self) -> Result<(), Error> {
        println!("ws client connected");

 
        let handle = self.spawn_metrics_task();
        self.handle = Some(handle);

        self.try_to_resend_failed_responses();
        
        Ok(())
    }

    async fn on_connect_fail(&mut self, error: WSError) -> Result<ClientCloseMode, Error> {
        eprintln!("ws client connection failed with error: {error:?}");
        
        Ok(ClientCloseMode::Reconnect)
    }

    async fn on_disconnect(&mut self) -> Result<ClientCloseMode, Error> {
        println!("ws client disconnected");
        
        if let Some(handle) = self.handle.take() {
            println!("Aborting metrics task");
            handle.abort();
            let _ = handle.await;
            println!("Metrics task stopped");
        }
        
        Ok(ClientCloseMode::Reconnect)
    }
}

impl WsClientActor {
    async fn incoming_msg_handler(&mut self, incoming_msg: IncomingMessage) -> String {

        let res: OutgoingResponse = match incoming_msg.command {

            IncomingCommand::ShellExecute(run_sh_cmd_req) => {
                let res = run_command(self.app_state.clone(), run_sh_cmd_req).await;
                
                OutgoingResponse::ShellExecute(res)
            }

        };
        
        
        let outgoing_msg = OutgoingMessage {
            message_id: incoming_msg.message_id,
            message: res
        };
        
        
        let serialized_msg = serde_json::to_string(&outgoing_msg)
            .unwrap_or_else(|_| "Error parsing response to JSON".to_string());
        
        
        serialized_msg
    }
    
    fn spawn_metrics_task(&self) -> JoinHandle<()> {
        let tx = self.tx_to_ws.clone();
        let app_state = self.app_state.clone();
        
        let handle = tokio::spawn(async move {
            println!("Metrics sending task spawned");

            loop {
                WsClientActor::send_metrics(&app_state, &tx);
                
                println!("Metrics sent");
                
                tokio::time::sleep(Duration::from_secs(10)).await;
            }
        
        });
        
        handle
    }
    
    
    fn try_to_resend_failed_responses(&mut self) {
        if !self.res_failed_to_send.is_empty() {
            let res_count = self.res_failed_to_send.len();
            
            println!("Sending responses that failed to send earlier: {}", res_count);


            self.res_failed_to_send.retain(|msg| {
                match self.tx_to_ws.text(msg) {
                    Ok(_) => {
                        println!("Successfully re-sent message to websocket");
                        false
                    },
                    Err(_) => true
                }
            });
        }
    }
    
    fn send_metrics(app_state: &Arc<AppState>, tx_to_ws: &Client<Self>) {
        let metrics: SysMetrics = app_state.sysinfo_service.get_all_metrics();
        
        let message_id = String::new();
        let msg = OutgoingMessage {message_id, message: OutgoingResponse::Metrics(metrics)};
        
        let serialized = serde_json::to_string(&msg)
            .unwrap_or_else(|_| "Error serializing metrics".to_string());
        
        if let Err(err) =tx_to_ws.text(serialized) {
            eprintln!("Error sending metrics: {err}");
        }
    }
    
}


