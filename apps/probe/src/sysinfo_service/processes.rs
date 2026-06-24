use sysinfo::{Pid, ProcessRefreshKind, ProcessesToUpdate, System};

#[derive(Debug)]
pub struct ProcessMetrics{
    pub name: String,
    pub used_memory: u64,
    pub cpu_usage: f32,
    pub run_time: u64,
    
}

pub fn collect_process_metrics(sys: &mut System, pid: Pid) -> ProcessMetrics {
    
    
    sys.refresh_processes_specifics(ProcessesToUpdate::Some(&[pid]), false,
                                    ProcessRefreshKind::everything().without_tasks());
    
    let process = sys.process(pid).unwrap();
    
    let name = process.name().to_string_lossy().into_owned(); 
    let used_memory = process.memory();
    let cpu_usage = process.cpu_usage();
    let run_time = process.run_time();



    ProcessMetrics { name, used_memory, cpu_usage, run_time }
}
