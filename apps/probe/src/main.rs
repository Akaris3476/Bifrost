mod api;
pub mod services;

use std::{thread::sleep, time::Duration};
use services::sysinfo_service::SysInfoService;
use crate::api::ApiService;

#[tokio::main]
async fn main() {

    
    let mut api_service = 
        ApiService::new(String::from("localhost:3476"),
                         String::from("api_key_placeholder")
                        );
    
    
    api_service.connect_to_server(Duration::from_secs(5)).await;
    
    // api::handlers::run_command(ShellCommands::Install).await;
    
    let sysinfo_service = SysInfoService::new();

    loop {
        // let metrics = sysinfo_service.get_all_metrics();

        // println!("Usage of each cpu core:\n {:#?}\n", metrics.cpu.cores_usage);
        // println!("Average cpu usage:\n {:.2}%\n", metrics.cpu.avg_usage);
        // 
        // println!("This process:\n{:#?}\n", metrics.probe_process);
        // println!("Disks:\n{:#?}\n", metrics.disks);
        // println!("Memory:\n{:#?}\n", metrics.memory);
        // println!("Components:\n{:#?}\n", metrics.components);
        println!("-----------------------------------------------");
        
        // api_controller.send_metrics(&metrics).await.unwrap_or_else(|err| println!("Send metrics failed: {:?}", err));


        sleep(Duration::from_secs(1));
    }
}
