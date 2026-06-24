use sysinfo::{System};


#[derive(Debug)]
pub struct CpuMetrics {
    pub cores_usage: Box<[f32]>,
    pub avg_usage: f32,
    
}

pub  fn collect_cpu_metrics(sys: &mut System) -> CpuMetrics {
    let cores_usage = get_cores_usage(sys);
    let avg_usage = avg_cpu_usage(&cores_usage);

    CpuMetrics { cores_usage, avg_usage }
}

fn avg_cpu_usage(cpus_usage: &[f32]) -> f32 {
    let avg_usage = cpus_usage.iter().sum::<f32>();
    let cpu_count = cpus_usage.len();
    
    avg_usage / cpu_count as f32
}

fn get_cores_usage(sys: &mut System) -> Box<[f32]> {
    sys.refresh_cpu_usage();
    
    let cpus_count = sys.cpus().len();
    let mut cpus: Vec<f32> = Vec::with_capacity(cpus_count);
    
    for cpu in sys.cpus() {
        cpus.push(cpu.cpu_usage());
    }
    
    cpus.into_boxed_slice()
    
}