use std::sync::Mutex;
use sysinfo::{get_current_pid, Components, Disks, System};
use super::{cpu::*, disk::*, components::*, memory::*, processes::*, };
use std::time::{SystemTime};
use serde::{Deserialize, Serialize};

#[derive(Debug)]
#[derive(Serialize, Deserialize)]
pub struct SysMetrics {
    pub timestamp: SystemTime,
    pub cpu: CpuMetrics,
    pub memory: MemoryMetrics,
    pub probe_process: ProcessMetrics,
    pub disks: Box<[DiskMetrics]>,
    pub components: ComponentsMetrics
}

pub struct SysInfoService{
    inner: Mutex<SysInfoInner>
}

pub struct SysInfoInner {
    sys: System,
    disks: Disks,
    components: Components
}

impl SysInfoService {

    pub fn new() -> Self {
        let sys = System::new_all();
        let disks = Disks::new_with_refreshed_list();
        let components = Components::new_with_refreshed_list();
        
        SysInfoService { 
            inner: Mutex::new(SysInfoInner{
                sys, disks, components
            })
        }
    }
    
    pub fn get_all_metrics(&self) -> SysMetrics {
        let mut inner = self.inner.lock().unwrap();
        
        let now = SystemTime::now();
        
        let cpu_metrics = collect_cpu_metrics(&mut inner.sys);
        let memory_metrics = collect_memory_metrics(&mut inner.sys);
        let probe_metrics = {
            let probe_pid = get_current_pid().unwrap();
            collect_process_metrics(&mut inner.sys, probe_pid)
        };
        let disc_metrics = collect_disks_metrics(&mut inner.disks);
        let components_metrics = collect_components_metrics(&mut inner.components);
        
        SysMetrics {
            timestamp: now,
            cpu: cpu_metrics,
            memory: memory_metrics,
            probe_process: probe_metrics,
            disks: disc_metrics,
            components: components_metrics
        }
        
    }
    
    pub fn _get_cpu_metrics(&self) -> CpuMetrics {
        let mut inner = self.inner.lock().unwrap();
        collect_cpu_metrics(&mut inner.sys)
    }
    
    pub fn _get_memory_metrics(&self) -> MemoryMetrics {
        let mut inner = self.inner.lock().unwrap();
        collect_memory_metrics(&mut inner.sys)
    }
    
    pub fn _get_disks_metrics(&self) -> Box<[DiskMetrics]> {
        let mut inner = self.inner.lock().unwrap();
        collect_disks_metrics(&mut inner.disks)
    }
    
    pub fn _get_components_metrics(&self) -> ComponentsMetrics {
        let mut inner = self.inner.lock().unwrap();
        collect_components_metrics(&mut inner.components)
    }
    
    pub  fn _get_probe_metrics(&self) -> ProcessMetrics {
        let mut inner = self.inner.lock().unwrap();
        let probe_pid = get_current_pid().unwrap();
        collect_process_metrics(&mut inner.sys, probe_pid)
    } 

    
}