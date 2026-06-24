mod disk;
mod service;
mod cpu;
mod memory;
mod processes;
mod components;

pub use service::SysInfoService;


// Possible TODOs:

// println!("System name:             {:?}", System::name());
// println!("System kernel version:   {:?}", System::kernel_version());
// println!("System OS version:       {:?}", System::os_version());
// println!("System host name:        {:?}", System::host_name());

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

// List of processes?
// Specify process to examine?