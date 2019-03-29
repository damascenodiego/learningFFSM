list.of.packages <- c("ggpubr","ggrepel","ggplot2","effsize")

# new.packages <- list.of.packages[!(list.of.packages %in% installed.packages(lib.loc="/home/cdnd1/Rpackages/")[,"Package"])]
# if(length(new.packages)) install.packages(new.packages,lib="/home/cdnd1/Rpackages/")
# lapply(list.of.packages,require,character.only=TRUE, lib.loc="/home/cdnd1/Rpackages/")

new.packages <- list.of.packages[!(list.of.packages %in% installed.packages()[,"Package"])]
if(length(new.packages)) install.packages(new.packages, dependencies = TRUE)
lapply(list.of.packages,require,character.only=TRUE)

rm(new.packages,list.of.packages)

tab<-read.table("dataset.tab",sep="\t", header=TRUE)
corrMethod<-"pearson"
  
plot<-ggscatter(tab,
                x = "RatioStates",
                xlab = "Ratio between FFSM size to the total size of the products merged",
                y = "RatioFeatures",
                ylab = "Amount of feature sharing",
                title = "Pearson correlation coefficient for FFSM size and Feature sharing",
                add = "reg.line",
                cor.coeff.args = list(method = corrMethod, label.x.npc = 0.6, label.y.npc = 0.9),
                conf.int = TRUE, # Add confidence interval
                cor.coef = TRUE # Add correlation coefficient. see ?stat_cor
)+
  theme_bw()+
  theme(
  plot.title = element_text(hjust = 0.5, size=9),
  axis.text.x  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=8),
  axis.text.y  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=8),
  axis.title.x  = element_text(angle = 0,  hjust = 0.5, vjust = 0.5, size=8),
  axis.title.y  = element_text(angle = 90, hjust = 0.5, vjust = 0.5, size=8),
)

plot
filename <- "correlation.pdf"
ggsave(device=cairo_pdf, filename, width = 8, height = 4, dpi=320)  # ssh plots  

##########################################################################

tab_recov<-read.table("recovering_ffsm.tab",sep="\t", header=TRUE)
tab_recov[,"Products.Analyzed"]<-tab_recov[,"Products.Analyzed"]*100
plot <- ggplot(data=tab_recov, aes_string(x="Products.Analyzed", y="StatesFFSM",shape="SPL")) +
  geom_line(stat = "identity",aes_string(linetype="SPL"))+
  labs(
    y = "FFSM size",
    x = "Percentage of products analyzed",
    title = "Size of the FFSMs recovered"
    )+
  scale_x_continuous(breaks = seq(0,100,10)) +
  scale_y_continuous(breaks = seq(0,15,1))+
  theme_bw()+
  geom_hline(colour="gray", yintercept=6,linetype="solid") + 
  geom_hline(colour="gray", yintercept=14,linetype="dashed") + 
  geom_hline(colour="gray", yintercept=13,linetype="longdash") + 
  annotate("text",x = 90, y = 6, label="Hand-crafted AGM" , size = 3)+
  annotate("text",x = 90, y = 14, label="Hand-crafted VM" , size = 3)+
  annotate("text",x = 90, y = 13, label="Hand-crafted WS" , size = 3)+
  theme(
    # plot.title = element_text(hjust = 0.5, size=9),
    plot.title = element_blank(),
    legend.position="top",
    axis.text.x  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=8),
    axis.text.y  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=8),
    axis.title.x  = element_text(angle = 0,  hjust = 0.5, vjust = 0.5, size=8),
    axis.title.y  = element_text(angle = 90, hjust = 0.5, vjust = 0.5, size=8)
  )
  
plot
filename <- "recovering_ffsm.pdf"
ggsave(device=cairo_pdf, filename, width = 6, height = 4, dpi=320)  # ssh plots  


  

# spl_name<-tab_recov$SPL
spl_name<-gsub("_[0-9]+\\.[a-z]+$","",tab$Reference)
spl_name<-toupper(gsub("^fsm_","",spl_name))
tot_prod_siz<-tab$TotalStatesRef+tab$TotalStatesUpdt
ffsm_siz<-tab$StatesFFSM
tab_prods<-data.frame(SPL=spl_name,Size=ffsm_siz,Model=rep("FFSM",length(spl_name)))
tab_prods<-rbind(tab_prods,setNames(data.frame(spl_name,tot_prod_siz,rep("All products",length(spl_name))),names(tab_prods)))

plot <- ggplot(data=tab_prods, aes_string(x="SPL",y="Size",color="Model")) + 
  geom_boxplot()+ 
  stat_boxplot(geom ='errorbar')+
  geom_hline(colour="gray", yintercept=6,linetype="solid") + 
  geom_hline(colour="gray", yintercept=14,linetype="dashed") + 
  geom_hline(colour="gray", yintercept=13,linetype="longdash") + 
  annotate("text",x = 0.650, y = 6, label="Hc AGM" , size = 2)+
  annotate("text",x = 1.550, y = 14, label="Hc VM" , size = 2)+
  annotate("text",x = 2.750, y = 13, label="Hc WS" , size = 2)+
  # scale_y_continuous(limits=c(0,30),breaks=seq(0,30,5))+
  scale_y_continuous(breaks=seq(0,30,5))+
  scale_fill_grey() + 
  theme_bw()+
  labs(x = "Software product line", y = "Number of states")
plot
filename <- "tot_size_prod.pdf"
ggsave(device=cairo_pdf, filename, width = 6, height = 3, dpi=320)  # ssh plots  


ffsm_orig_size<-list()
ffsm_orig_size[["AGM"]]<-21
ffsm_orig_size[["VM"]]<-14
ffsm_orig_size[["WS"]]<-13
for (variable in unique(spl_name)) {
  tmp_tab<-tab_prods[tab_prods$SPL==variable,]
  wilc_val<-wilcox.test(tmp_tab[tmp_tab$Model=="All products","Size"],tmp_tab[tmp_tab$Model=="FFSM","Size"])
  print(paste(variable," p-value = ",wilc_val$p.value))
  vd_val<-VD.A(tmp_tab$Size,tmp_tab$Model)
  print(paste(variable," Â = ",vd_val$estimate,"(",vd_val$magnitude,")"))
}

for (variable in unique(spl_name)) {
  tmp_tab<-tab_prods[tab_prods$SPL==variable,]
  subtmp_tab<-tmp_tab[tmp_tab$Model=="FFSM",]
  subtmp_tab<-rbind(subtmp_tab,setNames(data.frame(rep(variable,length(subtmp_tab)),rep(ffsm_orig_size[[variable]],length(subtmp_tab)),"Original Size"),names(subtmp_tab)))
  subtmp_tab$Model<-droplevels(subtmp_tab$Model)
  wilc_val<-wilcox.test(subtmp_tab[subtmp_tab$Model=="FFSM","Size"],subtmp_tab[subtmp_tab$Model=="Original Size","Size"])
  print(paste(variable," p-value = ",wilc_val$p.value))
  vd_val<-VD.A(subtmp_tab$Size,subtmp_tab$Model)
  print(paste(variable," Â = ",vd_val$estimate,"(",vd_val$magnitude,")"))
}

