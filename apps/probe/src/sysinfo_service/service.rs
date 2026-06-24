use sysinfo::{get_current_pid, Components, Disks, System};
use super::{cpu::*, disk::*, components::*, memory::*, processes::*, };
use std::time::{SystemTime};

#[derive(Debug)]
pub struct SysMetrics {
    pub timestamp: SystemTime,
    pub cpu: CpuMetrics,
    pub memory: MemoryMetrics,
    pub probe_process: ProcessMetrics,
    pub disks: Box<[DiskMetrics]>,
    pub components: ComponentsMetrics
}

pub struct SysInfoService {
    sys: System,
    disks: Disks,
    components: Components
}

impl SysInfoService {

    pub fn new() -> Self {
        let sys = System::new_all();
        let disks = Disks::new_with_refreshed_list();
        let components = Components::new_with_refreshed_list();
        SysInfoService { sys, disks, components }
    }
    
    pub fn get_all_metrics(&mut self) -> SysMetrics {
        let now = SystemTime::now();
        
        let cpu_metrics = self.get_cpu_metrics();
        let memory_metrics = self.get_memory_metrics();
        let probe_metrics = self.get_probe_metrics();
        let disc_metrics = self.get_disks_metrics();
        let components_metrics = self.get_components_metrics();
        
        SysMetrics {
            timestamp: now,
            cpu: cpu_metrics,
            memory: memory_metrics,
            probe_process: probe_metrics,
            disks: disc_metrics,
            components: components_metrics
        }
        
    }
    
    pub fn get_cpu_metrics(&mut self) -> CpuMetrics {
        collect_cpu_metrics(&mut self.sys)
    }
    
    pub fn get_memory_metrics(&mut self) -> MemoryMetrics {
        collect_memory_metrics(&mut self.sys)
    }
    
    pub fn get_disks_metrics(&mut self) -> Box<[DiskMetrics]> {
        collect_disks_metrics(&mut self.disks)
    }
    
    pub fn get_components_metrics(&mut self) -> ComponentsMetrics {
        collect_components_metrics(&mut self.components)
    }
    
    pub  fn get_probe_metrics(&mut self) -> ProcessMetrics {
        let probe_pid = get_current_pid().unwrap();
        collect_process_metrics(&mut self.sys, probe_pid)
    } 

    
}