mod sysinfo_service;
mod api;

use std::{sync::Arc, thread::sleep, time::Duration};
use sysinfo_service::SysInfoService;
use crate::api::ApiService;

#[tokio::main]
async fn main() {

    let mut sysinfo_service = SysInfoService::new();
    let api_controller = Arc::new(ApiService::new(String::from("http://localhost:3476")));
    let api_listener = api_controller.clone();
    
    tokio::spawn(async move {
        api_listener.listen().await;
    });
    
    // api::handlers::run_command(ShellCommands::Install).await;

    loop {
        let metrics = sysinfo_service.get_all_metrics();

        // println!("Usage of each cpu core:\n {:#?}\n", metrics.cpu.cores_usage);
        // println!("Average cpu usage:\n {:.2}%\n", metrics.cpu.avg_usage);
        // 
        // println!("This process:\n{:#?}\n", metrics.probe_process);
        // println!("Disks:\n{:#?}\n", metrics.disks);
        // println!("Memory:\n{:#?}\n", metrics.memory);
        // // println!("Components:\n{:#?}\n", metrics.components);
        println!("-----------------------------------------------");
        
        // api_controller.send_metrics(&metrics).await.unwrap_or_else(|err| println!("Send metrics failed: {:?}", err));


        sleep(Duration::from_secs(1));
    }
}
