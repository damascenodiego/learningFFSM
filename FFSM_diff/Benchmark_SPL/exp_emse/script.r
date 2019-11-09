list.of.packages <- c("ggpubr","ggrepel","ggplot2","effsize","stringr","reshape","dplyr", "egg", "gridExtra", "xtable")

# new.packages <- list.of.packages[!(list.of.packages %in% installed.packages(lib.loc="/home/cdnd1/Rpackages/")[,"Package"])]
# if(length(new.packages)) install.packages(new.packages,lib="/home/cdnd1/Rpackages/")
# lapply(list.of.packages,require,character.only=TRUE, lib.loc="/home/cdnd1/Rpackages/")

new.packages <- list.of.packages[!(list.of.packages %in% installed.packages()[,"Package"])]
if(length(new.packages)) install.packages(new.packages, dependencies = TRUE)
lapply(list.of.packages,require,character.only=TRUE)

rm(new.packages,list.of.packages)

# # Loading log files from pair merging, dissimilarity, and comparelanguage.
# # Then, combine tables in a single data structure
# pair_tabs <- NULL
# # for (an_spl in c("agm", "bcs2")) {
# # for (an_spl in c("agm", "vm", "ws", "bcs2")) {
# # for (an_spl in c("agm", "vm", "ws", "bcs2", "cpterminal", "minepump")) {
# for (an_spl in c("agm", "vm", "ws", "cpterminal", "minepump", "aerouc5")) {
# # for (an_spl in c("agm", "vm", "ws", "bcs2", "cpterminal", "minepump", "aerouc5")) {
#   tab_c<-read.table(paste("./pair/pair_merging_",an_spl,".log",sep = ""),sep="/", header=TRUE)
#   tab_d<-read.table(paste("./pair/pair_dissimilarity_",an_spl,".log",sep = ""),sep="\t", header=TRUE)
#   tab_l<-read.table(paste("./pair/pair_comparelanguages_",an_spl,".log",sep = ""),sep="|", header=TRUE)
#   tab_m<-merge(merge(tab_c,tab_d),tab_l)
#   tab_m$SPL <- an_spl
#   if(is.null(pair_tabs)){
#     pair_tabs <- tab_m
#   }else{
#     pair_tabs <- rbind(pair_tabs,tab_m)
#   }
#   rm(tab_c,tab_d,tab_l,tab_m)
# }
# write.table(pair_tabs,"./pair_mdc.tab")
pair_tabs<-read.table("./pair_mdc.tab")
pair_tabs$SPL<-toupper(pair_tabs$SPL)
pair_tabs$SPL <- factor(pair_tabs$SPL, levels = c("AGM","VM","WS","AEROUC5","CPTERMINAL","MINEPUMP"))

# calculate similarity (opposite of dissim)
pair_tabs$ConfigSim <- 1-pair_tabs$ConfigDissim

# calculate model size increment in terms of the maximum size (number of states)
pair_tabs$RatioStates<- pair_tabs$StatesFFSM/apply(pair_tabs[,c("TotalStatesRef","TotalStatesUpdt")],1,sum)

# calculate model size increment in terms of the maximum size (number of transitions)  
pair_tabs$RatioTransitions<- pair_tabs$TransitionsFFSM/apply(pair_tabs[,c("TotalTransitionsRef","TotalTransitionsUpdt")],1,sum)

# # calculate model size increment in terms of the largest model size (number of states)
# pair_tabs$RatioStatesMax<- pair_tabs$StatesFFSM/apply(pair_tabs[,c("TotalStatesRef","TotalStatesUpdt")],1,max)
# 
# # calculate model size increment in terms of the largest model size (number of transitions)  
# pair_tabs$RatioTransitionsMax<- pair_tabs$TransitionsFFSM/apply(pair_tabs[,c("TotalTransitionsRef","TotalTransitionsUpdt")],1,max)

# dissimilarity histogram 
filename <- "histogram_ConfigSim.pdf"
p<-ggplot(pair_tabs, aes(x=ConfigSim)) + 
  geom_histogram(color="black", fill="lightgrey", bins=7)+
  labs(x = "Configuration similarity", y = "Frequency")+
  theme_bw()+
  theme(
    plot.title = element_text(hjust = 0.5, size=10),
    axis.text.x  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
    axis.text.y  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
    axis.title.x  = element_text(angle = 0,  hjust = 0.5, vjust = 0.5, size=10),
    axis.title.y  = element_text(angle = 90, hjust = 0.5, vjust = 0.5, size=10)
  )+facet_wrap(SPL~.,scales = "free_y", nrow = 2)
#print(p)
ggsave(device=cairo_pdf, filename, width = 7.250, height = 4.5, dpi=320,p)
rm(p,filename)

# dissimilarity (RatioStates) histogram
filename <- "histogram_RatioStates.pdf"
p<-ggplot(pair_tabs, aes(x=RatioStates)) +
  geom_histogram(color="black", fill="lightgrey", bins=7)+
  labs(x = "Ratio between FFSM size to total size of products pairs (number of states)", y = "Frequency")+
  theme_bw()+
  theme(
    plot.title = element_text(hjust = 0.5, size=10),
    axis.text.x  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
    axis.text.y  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
    axis.title.x  = element_text(angle = 0,  hjust = 0.5, vjust = 0.5, size=10),
    axis.title.y  = element_text(angle = 90, hjust = 0.5, vjust = 0.5, size=10)
  )+facet_wrap(SPL~.,scales = "free_y", nrow = 2)
#print(p)
ggsave(device=cairo_pdf, filename, width = 7.250, height = 4.5, dpi=320,p)
rm(p,filename)

# dissimilarity (RatioTransitions) histogram
filename <- "histogram_RatioTransitions.pdf"
p<-ggplot(pair_tabs, aes(x=RatioTransitions)) +
  geom_histogram(color="black", fill="lightgrey", bins=7)+
  labs(x = "Ratio between FFSM size to total size of products pairs (number of transitions)", y = "Frequency")+
  theme_bw()+
  theme(
    plot.title = element_text(hjust = 0.5, size=10),
    axis.text.x  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
    axis.text.y  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
    axis.title.x  = element_text(angle = 0,  hjust = 0.5, vjust = 0.5, size=10),
    axis.title.y  = element_text(angle = 90, hjust = 0.5, vjust = 0.5, size=10)
  )+facet_wrap(SPL~.,scales = "free_y", nrow = 2)
#print(p)
ggsave(device=cairo_pdf, filename, width = 7.250, height = 4.5, dpi=320,p)
rm(p,filename)

{
  corrMethod<-"pearson"
  # x_col = "RatioStates";          xlab_txt = "Ratio between total sizes of FFSM to products pair (number of states)"
  x_col = "RatioTransitions";     xlab_txt = "Ratio between total sizes of FFSM to products pair (number of transitions)"
  y_col <- "ConfigSim"; ylab_txt <- "Configuration similarity";
  # y_col <- "RatioFeatures"; ylab_txt <- "Amount of feature sharing";
  
  y_title <- "Pearson correlation coefficient - Pairwise analysis"
  filename <- paste("correlation_",x_col,"_",y_col,".pdf",sep = "")
  
  p <-ggscatter(pair_tabs,
                 x = x_col, xlab = NULL,
                 y = y_col, ylab = ylab_txt,
                 title = y_title,
                 add = "reg.line",
                 cor.coeff.args = list(method = corrMethod, label.x.npc = 0.00, label.y.npc = 0.00),
                 cor.coef.size = 4,
                 conf.int = TRUE, # Add confidence interval
                 cor.coef = TRUE # Add correlation coefficient. see ?stat_cor
  )+
    theme_bw()+
    theme(
      plot.title = element_text(hjust = 0.5, size=15),
      strip.text.x = element_text(size = 15),
      strip.text.y = element_text(size = 15),
      axis.text.x  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=15),
      axis.text.y  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=15),
      axis.title.x  = element_text(angle = 0,  hjust = 0.5, vjust = 0.5, size=15),
      axis.title.y  = element_text(angle = 90, hjust = 0.5, vjust = 0.5, size=15)
    )+ facet_wrap(SPL~.,scales = "free", nrow = 2)
  #print(p)
  ggsave(device=cairo_pdf, filename, width = 12.5, height = 6, dpi=320,p)
  rm(p,filename,x_col,xlab_txt,y_col,ylab_txt,y_title,corrMethod)
}

# # Loading report files of recovering FFSMs from prioritized configurations
# # Then, combine tables in a single data structure
# prtz_tabs <- NULL
# for (an_spl in c("agm", "vm", "ws", "cpterminal", "minepump", "aerouc5")) {
#   for (wise in c("1wise", "2wise", "3wise", "4wise", "all")) {
#     # tab_r<-read.table(paste("../",an_spl,"/products_",wise,"/","report.tab",sep = ""),sep="/", header=TRUE)
#     # tab_prf<-read.table(paste("../",an_spl,"/products_",wise,"/","report_prf.tab",sep = ""),sep="|", header=TRUE)
#     tab_l<-read.table(paste("../",an_spl,"/products_",wise,"/","report_fmeasure_l.tab",sep = ""),sep="|", header=TRUE)
#     tab_l$SPL <- an_spl
#     tab_l$Twise <- wise
#     if(is.null(prtz_tabs)) prtz_tabs <- tab_l
#     else{
#       prtz_tabs <- rbind(prtz_tabs,tab_l)
#     }
#     rm(tab_l)
#   }
# }
# write.table(prtz_tabs,"./recov_prtz.tab")
prtz_tabs<-read.table("./recov_prtz.tab")
prtz_tabs$Index <-prtz_tabs$Reference
prtz_tabs$Index<-sub("^ffsm_","",prtz_tabs$Index)
prtz_tabs$Index<-sub("_kiss.txt$","",prtz_tabs$Index)
prtz_tabs$Index<-as.integer(prtz_tabs$Index)
prtz_tabs$SPL<-toupper(prtz_tabs$SPL)
prtz_tabs$SPL <- factor(prtz_tabs$SPL, levels = c("AGM","VM","WS","AEROUC5","CPTERMINAL","MINEPUMP"))

final_ffsm <- prtz_tabs %>% 
  group_by(SPL, Twise) %>%
  filter(Index == max(Index))

for (a_metric in c('Precision')) {
# for (a_metric in c('Precision', 'Recall','F.measure')) {
  filename <- paste(a_metric,".pdf",sep="")
  p <- ggplot(data=final_ffsm, aes_string(x="Twise",y=a_metric)) +
    geom_boxplot(color = "black", outlier.color = "red", outlier.size = .75)+
    stat_boxplot(geom ='errorbar',color = "black")+
    labs(x=NULL)+
    scale_fill_brewer(palette="Greys") +
    scale_y_continuous(limits = c(NA, 1))+
    theme_bw() +facet_wrap(SPL~.,scales = "free", nrow = 2) 
  #print(p)
  ggsave(device=cairo_pdf, filename, width = 8.65, height = 4.5, dpi=320,p)
  
  for(an_spl in unique(prtz_tabs$SPL)){
    filename <- paste(a_metric,"_",an_spl,"_byIndex.pdf",sep="")
    p <- ggplot(data=prtz_tabs[prtz_tabs$SPL==an_spl,], aes_string(x="Index",y=a_metric,group="Index")) +
      geom_boxplot(color = "black", outlier.color = "red", outlier.size = .75)+
      stat_boxplot(geom ='errorbar',color = "black")+
      scale_fill_brewer(palette="Greys") +
      scale_y_continuous(limits = c(NA, 1))+
      theme_bw() +facet_wrap(Twise~.,scales = "free", nrow = 1) 
    # print(p)
    ggsave(device=cairo_pdf, filename, width = 15, height = 3, dpi=320,p)
    # rm(p,a_metric,filename)
    
  }
}



final_by_index <- prtz_tabs %>% 
  group_by(SPL,Twise,Index) %>%
  summarize(
    mean   = mean(Precision),
    # median = median(Precision),
    sd     = sd(Precision),    
    min    = min(Precision),
    max    = max(Precision),
    count  = length(Precision)
    
    # q1     = quantile(Precision,0.25),
    # q2     = quantile(Precision,0.50),
    # q3     = quantile(Precision,0.75),
    # q4     = quantile(Precision,1.00)
  )
summarized_final <- final_by_index %>% 
  mutate_if(is.numeric, round, 2)
summarized_final<-t(summarized_final)
write.table(summarized_final,"./final_by_index.tab")


cat("",file="statistics.txt",append=FALSE)
for (an_spl in c("AGM","VM","WS","AEROUC5","CPTERMINAL","MINEPUMP")) {
  cat(paste("#########",an_spl),sep = "\n",file="statistics.txt",append=TRUE)
  
  wise1 <- final_ffsm[final_ffsm$SPL==an_spl & final_ffsm$Twise=="1wise",]$Precision
  wise2 <- final_ffsm[final_ffsm$SPL==an_spl & final_ffsm$Twise=="2wise",]$Precision
  wise3 <- final_ffsm[final_ffsm$SPL==an_spl & final_ffsm$Twise=="3wise",]$Precision
  wise4 <- final_ffsm[final_ffsm$SPL==an_spl & final_ffsm$Twise=="4wise",]$Precision
  wisea <- final_ffsm[final_ffsm$SPL==an_spl & final_ffsm$Twise=="all"  ,]$Precision
  
  # wilcox test
  wilc_w12 <-wilcox.test(wise1,wisea)
  wilc_w23 <-wilcox.test(wise2,wisea)
  wilc_w34 <-wilcox.test(wise3,wisea)
  wilc_w4a <-wilcox.test(wise4,wisea)
  
  # Vargha-Delaney
  vd_12 <- VD.A(wise1,wisea)
  vd_23 <- VD.A(wise2,wisea)
  vd_34 <- VD.A(wise3,wisea)
  vd_4a <- VD.A(wise4,wisea)
  
  
  cat("1-wise vs all",file="statistics.txt",sep="\n",append=TRUE)
  cat(paste("\tp-value =",wilc_w12$p.value),file="statistics.txt",sep="\n",append=TRUE)
  cat(paste("\tÂ =",vd_12$estimate,"(",vd_12$magnitude,")"),file="statistics.txt",sep="\n",append=TRUE)
  cat("2-wise vs all",file="statistics.txt",sep="\n",append=TRUE)
  cat(paste("\tp-value =",wilc_w23$p.value),file="statistics.txt",sep="\n",append=TRUE)
  cat(paste("\tÂ =",vd_23$estimate,"(",vd_23$magnitude,")"),file="statistics.txt",sep="\n",append=TRUE)
  cat("3-wise vs all",file="statistics.txt",sep="\n",append=TRUE)
  cat(paste("\tp-value =",wilc_w34$p.value),file="statistics.txt",sep="\n",append=TRUE)
  cat(paste("\tÂ =",vd_34$estimate,"(",vd_34$magnitude,")"),file="statistics.txt",sep="\n",append=TRUE)
  cat("4-wise vs all",file="statistics.txt",sep="\n",append=TRUE)
  cat(paste("\tp-value =",wilc_w4a$p.value),file="statistics.txt",sep="\n",append=TRUE)
  cat(paste("\tÂ =",vd_4a$estimate,"(",vd_4a$magnitude,")"),file="statistics.txt",sep="\n",append=TRUE)
}

summarized_final <- final_ffsm %>%
  group_by(SPL,Twise) %>%
  summarize(
    mean   = mean(Precision),
    # median = median(Precision),
    sd     = sd(Precision),    
    min    = min(Precision),
    max    = max(Precision),
    count  = length(Precision)
    
    # q1     = quantile(Precision,0.25),
    # q2     = quantile(Precision,0.50),
    # q3     = quantile(Precision,0.75),
    # q4     = quantile(Precision,1.00)
  )
summarized_final <- summarized_final %>% 
  mutate_if(is.numeric, round, 2)
summarized_final<-t(summarized_final)
write.table(summarized_final,"./summarized_final.tab")

