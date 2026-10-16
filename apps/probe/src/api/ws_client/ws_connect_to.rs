use crate::api::api_service::AppState;
use ezsockets::{Client, ClientConfig};
use std::sync::Arc;
use crate::api::ws_client::ws_client_actor::WsClientActor;


pub async fn ws_connect_to(remote_url: &str, api_key: &str, app_state: Arc<AppState>) -> Client<WsClientActor> {

    let connection_string: String = format!("ws://{}/ws?api_key={}", remote_url, api_key);

    let config = ClientConfig::new(connection_string.as_str());

    let (tx_to_ws, future) = ezsockets::connect(
        |tx_to_ws| { WsClientActor::new(tx_to_ws, app_state)},
        config).await;

    tokio::spawn(future);

    tx_to_ws

}