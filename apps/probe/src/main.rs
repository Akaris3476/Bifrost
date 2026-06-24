mod sysinfo_service;

use std::thread::sleep;
use std::time::Duration;
use sysinfo_service::SysInfoService;

fn main() {
    

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


    let mut sysinfo_service = SysInfoService::new();

    loop {
        let metrics = sysinfo_service.get_all_metrics();
        
        println!("Usage of each cpu core:\n {:#?}\n", metrics.cpu.cores_usage);
        println!("Average cpu usage:\n {:.2}%\n", metrics.cpu.avg_usage);
        
        println!("{:#?}", metrics.probe_process);

        
        sleep(Duration::from_secs(1));
    }
}
