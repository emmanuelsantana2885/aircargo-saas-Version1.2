package com.aircargo.loadplanningservice.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FlightLoadPlanDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String uld;
    private String pos;
    private String config;
    private String sello;
    private Double weight;
    private Double tara;
    private String status;
    private List<ItemDTO> items = new ArrayList<>();

    public FlightLoadPlanDTO() {}

    public static class ItemDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private String mawb;
        private Integer pcs;
        private Integer volumePct;
        private String description;
        private String destino;

        public ItemDTO() {}

        public String getMawb() { return mawb; }
        public void setMawb(String mawb) { this.mawb = mawb; }
        public Integer getPcs() { return pcs; }
        public void setPcs(Integer pcs) { this.pcs = pcs; }
        public Integer getVolumePct() { return volumePct; }
        public void setVolumePct(Integer volumePct) { this.volumePct = volumePct; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getDestino() { return destino; }
        public void setDestino(String destino) { this.destino = destino; }
    }

    public String getUld() { return uld; }
    public void setUld(String uld) { this.uld = uld; }
    public String getPos() { return pos; }
    public void setPos(String pos) { this.pos = pos; }
    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
    public String getSello() { return sello; }
    public void setSello(String sello) { this.sello = sello; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public Double getTara() { return tara; }
    public void setTara(Double tara) { this.tara = tara; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<ItemDTO> getItems() { return items; }
    public void setItems(List<ItemDTO> items) { this.items = items; }
}
