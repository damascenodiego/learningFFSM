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


############################
# Succinctness - plot #
############################

# boxplot pairs size 
filename <- "boxplot_pairs_transitions_size.pdf"
tmp_tab <- data.frame(SPL=pair_tabs$SPL,FFSM=pair_tabs$TransitionsFFSM,Pair=pair_tabs$TotalTransitionsRef+pair_tabs$TotalTransitionsUpdt)
# filename <- "boxplot_pairs_states_size.pdf"
# tmp_tab <- data.frame(SPL=pair_tabs$SPL,FFSM=pair_tabs$StatesFFSM,Pair=pair_tabs$TotalStatesRef+pair_tabs$TotalStatesUpdt)
tmp_tab <- melt(tmp_tab, id.vars = "SPL", measure.vars = c("FFSM", "Pair"))
colnames(tmp_tab) <- c("SPL","Model","Size")
tmp_tab$OriginalSize <- 0

# Original number of transitions
tmp_tab[tmp_tab$SPL=="AGM","OriginalSize"] <- 35
tmp_tab[tmp_tab$SPL=="VM","OriginalSize"]  <- 197
tmp_tab[tmp_tab$SPL=="WS","OriginalSize"]  <- 112
tmp_tab[tmp_tab$SPL=="AEROUC5","OriginalSize"]    <- 450
tmp_tab[tmp_tab$SPL=="CPTERMINAL","OriginalSize"] <- 176
tmp_tab[tmp_tab$SPL=="MINEPUMP","OriginalSize"]   <- 575

# # Original number of states
# tmp_tab[tmp_tab$SPL=="AGM","OriginalSize"] <- 6
# tmp_tab[tmp_tab$SPL=="VM","OriginalSize"]  <- 14
# tmp_tab[tmp_tab$SPL=="WS","OriginalSize"]  <- 13
# tmp_tab[tmp_tab$SPL=="AEROUC5","OriginalSize"]    <- 25
# tmp_tab[tmp_tab$SPL=="CPTERMINAL","OriginalSize"] <- 11
# tmp_tab[tmp_tab$SPL=="MINEPUMP","OriginalSize"]   <- 25

p<-ggplot(tmp_tab, aes(x=Model,y=Size,shape=Model,fill=Model)) + 
  geom_boxplot(color = "black")+ 
  stat_boxplot(geom ='errorbar',color = "black")+
  labs(x = "Software product line", y = "Number of transitions")+
  # labs(x = "Software product line", y = "Number of states")+
  theme_bw()+
  scale_fill_brewer(palette="Greys") +
  geom_segment(aes(x = 0.5, y = OriginalSize, xend = 2.5, yend = OriginalSize,colour = "red"),linetype="dashed")+
  theme(
    legend.position = "none",
    plot.title = element_text(hjust = 0.5, size=10),
    axis.text.x  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
    axis.text.y  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
    axis.title.x  = element_text(angle = 0,  hjust = 0.5, vjust = 0.5, size=10),
    axis.title.y  = element_text(angle = 90, hjust = 0.5, vjust = 0.5, size=10)
  )+facet_wrap(SPL~.,scales = "free", nrow = 2)
# print(p)
ggsave(device=cairo_pdf, filename, width = 6.0, height = 4.5, dpi=320,p)
rm(p,filename)

cat("",file="statistics_pairs_size.txt",append=FALSE)
for (an_spl in c("AGM","VM","WS","AEROUC5","CPTERMINAL","MINEPUMP")) {
  cat(paste("#########",an_spl),sep = "\n",file="statistics_pairs_size.txt",append=TRUE)
  
  sz_learnt <- tmp_tab[tmp_tab$SPL==an_spl & tmp_tab$Model=="FFSM",]$Size
  sz_origin <- tmp_tab[tmp_tab$SPL==an_spl & tmp_tab$Model=="FFSM",]$OriginalSize
  
  # wilcox test
  wilc_sz <-wilcox.test(sz_learnt,sz_origin)
  
  # Vargha-Delaney
  vd_sz <- VD.A(sz_learnt,sz_origin)
  
  cat(paste("\tp-value =",wilc_sz$p.value),file="statistics_pairs_size.txt",sep="\n",append=TRUE)
  cat(paste("\tÂ =",vd_sz$estimate,"(",vd_sz$magnitude,")"),file="statistics_pairs_size.txt",sep="\n",append=TRUE)
}
  rm(sz_learnt,sz_origin,wilc_sz,vd_sz)

######################################
# Succinctness - MW and Â tests #
######################################

cat("",file="pairs_size.txt",sep="",append=FALSE)
for(an_spl in c("AGM","VM","WS","CPTERMINAL","MINEPUMP","AEROUC5")){
# for(an_spl in unique(tmp_tab$SPL)){
  warning(paste("Calculating statistics for",an_spl,"..."))
  ffsm_sizes    <- tmp_tab[(tmp_tab$SPL==an_spl & tmp_tab$Model=="FFSM"),"Size"]
  pairs_totSize <- tmp_tab[(tmp_tab$SPL==an_spl & tmp_tab$Model=="Pair"),"Size"]
  # wilcox test
  wilc_test <-wilcox.test(ffsm_sizes,pairs_totSize)
  
  # Vargha-Delaney
  vd_test <- VD.A(ffsm_sizes,pairs_totSize)
  
  cat(paste("SPL name:",an_spl),file="pairs_size.txt",sep="\n",append=TRUE)
  cat(paste("\tp-value =",wilc_test$p.value),file="pairs_size.txt",sep="\n",append=TRUE)
  cat(paste("\tÂ =",vd_test$estimate,"(",vd_test$magnitude,")"),file="pairs_size.txt",sep="\n",append=TRUE)
}
rm(an_spl,tmp_tab,ffsm_sizes,pairs_totSize,wilc_test,vd_test)


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

##########################################
# Pearson's correlation coefficient #
##########################################
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
# rprf_tabs <- NULL
# for (an_spl in c("agm", "vm", "ws", "cpterminal", "minepump", "aerouc5")) {
#   for (wise in c("1wise", "2wise", "3wise", "4wise", "all")) {
#     tab_r<-read.table(paste("../",an_spl,"/products_",wise,"/","report.tab",sep = ""),sep="/", header=TRUE)
#     tab_prf<-read.table(paste("../",an_spl,"/products_",wise,"/","report_prf.tab",sep = ""),sep="|", header=TRUE)
#     tab_l<-read.table(paste("../",an_spl,"/products_",wise,"/","report_fmeasure_l.tab",sep = ""),sep="|", header=TRUE)
#     tab_l$SPL   <- an_spl;   tab_l$Twise <- wise;   tab_l$Index <- seq(nrow(tab_l))
#     tab_prf$SPL <- an_spl; tab_prf$Twise <- wise; tab_prf$Index <- seq(nrow(tab_prf))
# 
#     if(is.null(prtz_tabs)) {
#       prtz_tabs <- tab_l
#       rprf_tabs <- cbind(tab_r,tab_prf)
#     }else{
#       prtz_tabs <- rbind(prtz_tabs,tab_l)
#       rprf_tabs <- rbind(rprf_tabs,cbind(tab_r,tab_prf))
#     }
#     rm(tab_l,tab_r,tab_prf)
#   }
# }
# rm(an_spl,wise)
# write.table(prtz_tabs,"./recov_prtz.tab")
# write.table(rprf_tabs,"./recov_rprf.tab")


##################
# Precision 
##################
prtz_tabs<-read.table("./recov_prtz.tab")
prtz_tabs$Index <-prtz_tabs$Reference
prtz_tabs$Index<-sub("^ffsm_","",prtz_tabs$Index)
prtz_tabs$Index<-sub("_kiss.txt$","",prtz_tabs$Index)
prtz_tabs$Index<-as.integer(prtz_tabs$Index)
prtz_tabs$SPL<-toupper(prtz_tabs$SPL)
prtz_tabs$SPL <- factor(prtz_tabs$SPL, levels = c("AGM","VM","WS","AEROUC5","CPTERMINAL","MINEPUMP"))

final_ffsm <- prtz_tabs[c("SPL","Twise","Index")] %>% 
  group_by(SPL, Twise) %>%
  filter(Index == max(Index))
final_ffsm <- unique(final_ffsm)

##################
# Precision: T-wise sizes
##################
filename <- "twise_sizes.txt"
write.table(final_ffsm,filename, sep = "\t", row.names = FALSE)

filename <- "twise_sizes.pdf"
p<-ggplot(final_ffsm,aes(x=Twise,y=Index,fill=Twise))+
  geom_bar(stat = "identity",position = "dodge2", colour = "black")+
  labs(x=NULL,y="Number of configurations")+
  scale_fill_brewer(palette="Greys") +
  theme_bw() +
  theme(
    legend.position = "none",
    plot.title = element_text(hjust = 0.5, size=10),
    axis.text.x  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
    axis.text.y  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
    axis.title.x  = element_text(angle = 0,  hjust = 0.5, vjust = 0.5, size=10),
    axis.title.y  = element_text(angle = 90, hjust = 0.5, vjust = 0.5, size=10)
  )+facet_wrap(SPL~.,scales = "free", nrow = 2) 
#print(p)
ggsave(device=cairo_pdf, filename, width = 8.65, height = 4.5, dpi=320,p)



#########################
# Precision: Final #
#########################
final_ffsm <- prtz_tabs %>% 
  group_by(SPL, Twise) %>%
  filter(Index == max(Index))


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
write.table(summarized_final,"./precision_final.tab", sep = "\t", col.names = FALSE)


################################
# Precision: Intermediate #
################################
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
write.table(summarized_final,"./precision_intermediate.tab", sep = "\t", col.names = FALSE)


for (a_metric in c('Precision')) {
  # for (a_metric in c('Precision', 'Recall','F.measure')) {
  ###############################
  # Precision (final FFSM) #
  ###############################
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
  
  #######################################
  # Precision (intermediate FFSMs) #
  #######################################
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
    rm(p,filename)
  }
  rm(an_spl)
}
rm(a_metric)

###################################
# Precision - MW and Â tests #
###################################
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
rm(an_spl)
rm(vd_12,vd_23,vd_34,vd_4a)
rm(wise1,wise2,wise3,wise4,wisea)
rm(wilc_w12,wilc_w23,wilc_w34,wilc_w4a)
