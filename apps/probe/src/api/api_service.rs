use std::sync::Arc;
use ezsockets::Client;
use crate::api::ws_client::ws_client_actor::WsClientActor;
use crate::api::ws_client::ws_connect_to::ws_connect_to;
use crate::services::shell_service::ShellService;
use crate::services::sysinfo_service::{SysInfoService, SysMetrics};

pub struct   AppState {
    pub shell_service: ShellService,
    pub sysinfo_service: SysInfoService,
}
pub struct ApiService {
    remote_url: String,
    api_key: String,
    
    state: Arc<AppState>,
    tx_to_ws: Option<Client<WsClientActor>>
}

impl ApiService {
    
    pub fn new(remote_url: String, api_key: String) -> Self {
        let state = AppState {
            shell_service: ShellService::new(),
            sysinfo_service: SysInfoService::new(),
        };
        
        
        Self { state: Arc::new(state), api_key, remote_url, tx_to_ws: None }
    }
    
    pub async fn connect_to_server (&mut self)  {
        let tx_to_ws = ws_connect_to(&self.remote_url, &self.api_key, self.state.clone() ).await;
        self.tx_to_ws = Some(tx_to_ws);
    }
    
    
    // pub async fn send_metrics(&self, metrics: &SysMetrics)  {
    //     
    //     tokio::spawn(async move {
    // 
    //         self.state.sysinfo_service
    //         
    //         
    //         self.tx_to_ws.text(
    //             
    //         )
    // }
    
    
}
