use serde::{Deserialize, Serialize};
use sysinfo::{Components};

#[derive(Debug)]
#[derive(Serialize, Deserialize)]
pub struct ComponentMetrics {
    pub label: String,
    pub temp: f32
}
#[derive(Debug)]
#[derive(Serialize, Deserialize)]
pub struct ComponentsMetrics {
    pub components: Box<[ComponentMetrics]>,
    pub avg_temp: f32,
    pub max_temp: f32,
}


pub fn collect_components_metrics(components: &mut Components) -> ComponentsMetrics {
    components.refresh(true);
    
    let mut components_list: Vec<ComponentMetrics> = Vec::with_capacity(components.len());
    for component in components.iter() {
        let label = component.label().to_owned();
        let temp = component.temperature().unwrap_or(0.0);
        
        components_list.push(
            ComponentMetrics { label, temp }
        );
    }
    
    let components_metrics = components_list.into_boxed_slice();
    let avg_temp = calc_avg_temp(&components_metrics);
    let max_temp = calc_max_temp(&components_metrics);
    
    ComponentsMetrics { components: components_metrics, avg_temp, max_temp }

}

fn calc_avg_temp(components: &[ComponentMetrics]) -> f32 {
    if components.len() == 0 {
        return 0.0;
    }
    components.iter().map(|component| component.temp).sum::<f32>() 
        / components.len() as f32
}

fn calc_max_temp(components: &[ComponentMetrics]) -> f32 {
    components.iter().map(|component| component.temp)
        .max_by(|a, b| a.partial_cmp(b).unwrap())
        .unwrap_or(0.0)
}