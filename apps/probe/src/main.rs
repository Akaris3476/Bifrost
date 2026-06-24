mod sysinfo_service;

use std::thread::sleep;
use std::time::Duration;
use sysinfo_service::SysInfoService;

fn main() {

    let mut sysinfo_service = SysInfoService::new();

    loop {
        let metrics = sysinfo_service.get_all_metrics();

        println!("Usage of each cpu core:\n {:#?}\n", metrics.cpu.cores_usage);
        println!("Average cpu usage:\n {:.2}%\n", metrics.cpu.avg_usage);
        
        println!("This process:\n{:#?}\n", metrics.probe_process);
        println!("Disks:\n{:#?}\n", metrics.disks);
        println!("Memory:\n{:#?}\n", metrics.memory);
        // println!("Components:\n{:#?}\n", metrics.components);
        println!("-----------------------------------------------");


        sleep(Duration::from_secs(1));
    }
}
