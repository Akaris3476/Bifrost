use sysinfo::{get_current_pid, System};
use super::{cpu::*, disk, };
use std::time::{SystemTime};
use crate::sysinfo_service::memory::{collect_memory_metrics, MemoryMetrics};
use crate::sysinfo_service::processes::{collect_process_metrics, ProcessMetrics};

#[derive(Debug)]
pub struct SysMetrics {
    pub timestamp: SystemTime,
    pub cpu: CpuMetrics,
    pub memory: MemoryMetrics,
    pub probe_process: ProcessMetrics,
}

pub struct SysInfoService {
    sys: System,
}

impl SysInfoService {

    pub fn new() -> Self {
        let sys = System::new_all();
        // sys.refresh_all();

        SysInfoService { sys }
    }
    
    pub fn get_all_metrics(&mut self) -> SysMetrics {
        let now = SystemTime::now();
        
        let cpu_metrics = self.get_cpu_metrics();
        let memory_metrics = self.get_memory_metrics();
        let probe_metrics = self.get_probe_metrics();
        
        SysMetrics { timestamp: now, cpu: cpu_metrics, memory: memory_metrics, probe_process: probe_metrics }
        
    }
    
    pub fn get_cpu_metrics(&mut self) -> CpuMetrics {
        collect_cpu_metrics(&mut self.sys)
    }
    
    pub fn get_memory_metrics(&mut self) -> MemoryMetrics {
        collect_memory_metrics(&mut self.sys)
    }
    
    pub  fn get_probe_metrics(&mut self) -> ProcessMetrics {
        let probe_pid = get_current_pid().unwrap();
        collect_process_metrics(&mut self.sys, probe_pid)
    } 

    
}