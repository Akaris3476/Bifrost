use std::sync::Arc;
use std::time::Duration;
use reqwest::{Client, };
use axum::{serve,};
use tokio::net::TcpListener;
use crate::api::router::endpoints_urls_consts::Endpoints;
use crate::api::router::get_router;
use crate::api::shell_service::ShellService;
use crate::sysinfo_service::SysMetrics;

pub struct  AppState {
    client: Client,
    remote_url: String,
    pub shell_service: ShellService,
}
pub struct ApiService {
    state: Arc<AppState>,
}

impl ApiService {
    
    pub fn new(remote_url: String) -> Self {
        let state = AppState {
            client: Client::builder()
                .timeout(Duration::from_secs(15))
                .user_agent("Bifrost-Probe")
                .build()
                .unwrap(),
            remote_url,
            shell_service: ShellService {},
        };
        
        Self { state: Arc::new(state) }
    }
    
    pub async fn listen(&self) {

        let router = get_router(self.state.clone());
        let listener = get_listener(3476).await;
        
        serve(listener, router).await.unwrap();
    }
    
    
    pub async fn send_metrics(&self, metrics: &SysMetrics) -> Result<(), reqwest::Error> {
        let url = format!("{}{}", self.state.remote_url, Endpoints::SEND_METRICS);

        
        let res = self.state.client.post(&url)
            .json(metrics)
            .send()
            .await?;
        
        
            if res.status().is_success() {
                println!("Metrics sent successfully! {}", url);
            } else { 
                println!("Error sending metrics: {}", res.status());
            }
        
            Ok(())
        
    }
    
    
}


async fn get_listener(port: u16) -> TcpListener {
    let addr = format!("0.0.0.0:{}", port);

    let listener = TcpListener::bind(addr).await.unwrap();

    println!("Listening on {}", port);

    listener
}

