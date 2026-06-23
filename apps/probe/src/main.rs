mod sysinfo_service;

use std::thread::sleep;
use std::time::Duration;
use sysinfo_service::SysInfoService;

fn main() {
    
    // println!("=> system:");
    // // RAM and swap information:
    // println!("total memory: {} bytes", sys.total_memory());
    // println!("used memory : {} bytes", sys.used_memory());
    // println!("total swap  : {} bytes", sys.total_swap());
    // println!("used swap   : {} bytes", sys.used_swap());
    // 
    // // Display system information:
    // println!("System name:             {:?}", System::name());
    // println!("System kernel version:   {:?}", System::kernel_version());
    // println!("System OS version:       {:?}", System::os_version());
    // println!("System host name:        {:?}", System::host_name());
    
    // // Display processes ID, name and disk usage:
    // for (pid, process) in sys.processes() {
    //     println!("[{pid}] {:?} {:?}", process.name(), process.disk_usage());
    // }
    // 
    // // We display all disks' information:
    // println!("=> disks:");
    // let disks = Disks::new_with_refreshed_list();
    // for disk in &disks {
    //     println!("{disk:?}");
    // }
    // 
    // // Network interfaces name, total data received and total data transmitted:
    // let networks = Networks::new_with_refreshed_list();
    // println!("=> networks:");
    // for (interface_name, data) in &networks {
    //     println!(
    //         "{interface_name}: {} B (down) / {} B (up)",
    //         data.total_received(),
    //         data.total_transmitted(),
    //     );
    //     // If you want the amount of data received/transmitted since last call
    //     // to `Networks::refresh`, use `received`/`transmitted`.
    // }
    // 
    // // Components temperature:
    // let components = Components::new_with_refreshed_list();
    // println!("=> components:");
    // for component in &components {
    //     println!("{component:?}");
    // }
    // 
    // println!("=> Bifrost usage:");
    // let bifrost_pid = get_current_pid().unwrap();
    // println!("=> bifrost pid: {:?}", bifrost_pid);
    // let this_process = sys.process(bifrost_pid).unwrap();
    // println!("=> process: {:?}, {:?}", this_process.name(), this_process.memory()/1024);

    let mut sysinfo_service = SysInfoService::new();

    loop {
        let cpus_metrics = sysinfo_service.get_cpu_metrics();
        
        println!("Usage of each cpu core:\n {:#?}\n", cpus_metrics.cpus_usage);
        println!("Average cpu usage:\n {:.2}%\n", cpus_metrics.avg_usage);

        
        sleep(Duration::from_secs(1));
    }
}
