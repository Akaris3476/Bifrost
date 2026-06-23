use sysinfo::{System};
use super::{cpu::*, disk, };

pub struct SysInfoService {
    sys: System,
}

impl SysInfoService {

    pub fn new() -> Self {
        let mut sys = System::new_all();
        // sys.refresh_all();

        SysInfoService { sys }
    }
    
    pub fn get_cpu_metrics(&mut self) -> CpuMetrics {
        
        retrieve_cpu_metrics(&mut self.sys)
        
    }



}