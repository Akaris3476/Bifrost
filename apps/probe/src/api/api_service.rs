use std::sync::{Arc};
use std::time::Duration;
use crate::api::ws_client::connect_to::ws_connect_to;
use crate::api::WsTimeSettings;
use crate::config::ProbeConfig;
use crate::services::shell_service::ShellService;
use crate::services::sysinfo_service::{SysInfoService};

pub struct   AppState {
    pub shell_service: ShellService,
    pub sysinfo_service: SysInfoService,
    
    // tx_to_ws: Mutex<Option<
    //     Client<WsClientActor>
    // >>
}

// impl AppState{
//     pub fn set_tx_to_ws(&self, client: Client<WsClientActor>){
//             *self.tx_to_ws.lock().unwrap() = Some(client);
//     }
//     
//     pub fn get_tx_to_ws(&self) -> Option<Client<WsClientActor>> {
//         self.tx_to_ws.lock().ok()?.clone()
//     }
//     
//     
// }
pub struct ApiService {
    config: ProbeConfig,
    
    state: Arc<AppState>,
}

impl ApiService {
    
    pub fn new(config: ProbeConfig) -> Self {
        
        let state = AppState {
            shell_service: ShellService::new(),
            sysinfo_service: SysInfoService::new(),
            // tx_to_ws: None.into()
        };
        
        
        Self { state: Arc::new(state), config}
    }
    
    pub async fn connect_to_server (&mut self, ws_settings: WsTimeSettings)  {
        let _tx_to_ws = ws_connect_to(&self.config, &ws_settings, self.state.clone() ).await;
        // self.state.set_tx_to_ws(tx_to_ws);
    }

    
    
}
