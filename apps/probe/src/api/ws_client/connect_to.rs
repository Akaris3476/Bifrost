use crate::api::api_service::AppState;
use ezsockets::{Client, ClientConfig, SocketConfig};
use std::sync::Arc;
use std::time::Duration;
use crate::api::ws_client::actor::WsClientActor;


pub struct WsTimeSettings{
    pub(crate) reconnect_interval: Duration,
    pub(crate) heartbeat_interval: Duration,
    pub(crate) timeout: Duration,
}

pub async fn ws_connect_to(remote_url: &str, api_key: &str,
                           ws_settings: WsTimeSettings, app_state: Arc<AppState> ) -> Client<WsClientActor> {
    println!("Connecting to server {}", remote_url);
    
    let connection_string: String = format!("ws://{}/ws", remote_url);

    let mut socket_config = SocketConfig::default();
    socket_config.heartbeat = ws_settings.heartbeat_interval;
    socket_config.timeout = ws_settings.timeout;
    
    let config = ClientConfig::new(connection_string.as_str())
        .reconnect_interval(ws_settings.reconnect_interval)
        .header("X-API-Key", api_key)
        .socket_config(socket_config);

    let (tx_to_ws, future) = ezsockets::connect(
        |tx_to_ws| { WsClientActor::new(tx_to_ws, app_state)},
        config).await;

    tokio::spawn(future);

    tx_to_ws

}