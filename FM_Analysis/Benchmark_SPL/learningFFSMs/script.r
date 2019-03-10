list.of.packages <- c("ggpubr","ggrepel","ggplot2")

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

# plot
filename <- "correlation.pdf"
ggsave(device=cairo_pdf, filename, width = 8, height = 4, dpi=320)  # ssh plots  

##########################################################################

tab_recov<-read.table("recovering_ffsm.tab",sep="\t", header=TRUE)
tab_recov[,"Products.Analyzed"]<-tab_recov[,"Products.Analyzed"]*100
plot <- ggplot(data=tab_recov, aes_string(x="Products.Analyzed", y="StatesFFSM",shape="SPL")) +
  geom_line(stat = "identity",aes_string(linetype="SPL"))+
  labs(
    y = "FFSM size",
    x = "% of products analyzed",
    title = "Impact of cumulative learning on the FFSM size"
    )+
  scale_x_continuous(breaks = seq(0,100,10)) +
  scale_y_continuous(breaks = seq(0,15,1))+
  theme_bw()+
  theme(
    plot.title = element_text(hjust = 0.5, size=9),
    axis.text.x  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=8),
    axis.text.y  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=8),
    axis.title.x  = element_text(angle = 0,  hjust = 0.5, vjust = 0.5, size=8),
    axis.title.y  = element_text(angle = 90, hjust = 0.5, vjust = 0.5, size=8),
  )
  
# plot
filename <- "recovering_ffsm.pdf"
ggsave(device=cairo_pdf, filename, width = 6, height = 3, dpi=320)  # ssh plots  
