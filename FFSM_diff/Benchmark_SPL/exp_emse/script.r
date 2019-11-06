list.of.packages <- c("ggpubr","ggrepel","ggplot2","effsize","stringr","reshape","dplyr", "egg", "gridExtra")

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
# for (an_spl in c("agm", "vm", "ws", "bcs2", "cpterminal", "minepump", "aerouc5")) {
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

# calculate similarity (opposite of dissim)
pair_tabs$ConfigSim <- 1-pair_tabs$ConfigDissim

# calculate model size increment in terms of the maximum size (number of states)
pair_tabs$RatioStates<- apply(pair_tabs[,c("TotalStatesRef","TotalStatesUpdt")],1,sum)
pair_tabs$RatioStates<- pair_tabs$StatesFFSM/pair_tabs$RatioStates

# calculate model size increment in terms of the maximum size (number of transitions)  
pair_tabs$RatioTransitions<- apply(pair_tabs[,c("TotalTransitionsRef","TotalTransitionsUpdt")],1,sum)
pair_tabs$RatioTransitions<- pair_tabs$TransitionsFFSM/pair_tabs$RatioTransitions

# # calculate model size increment in terms of the largest model size (number of states)
# pair_tabs$RatioStatesMax<- apply(pair_tabs[,c("TotalStatesRef","TotalStatesUpdt")],1,max)
# pair_tabs$RatioStatesMax<- pair_tabs$StatesFFSM/pair_tabs$RatioStatesMax
# 
# # calculate model size increment in terms of the largest model size (number of transitions)  
# pair_tabs$RatioTransitionsMax<- apply(pair_tabs[,c("TotalTransitionsRef","TotalTransitionsUpdt")],1,max)
# pair_tabs$RatioTransitionsMax<- pair_tabs$TransitionsFFSM/pair_tabs$RatioTransitionsMax

pair_tabs$SPL<-toupper(pair_tabs$SPL)
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
  )+facet_wrap(SPL~.,scales = "free_y", nrow = 1)
p1 <- p %+% subset(pair_tabs, SPL %in% c("AGM","BCS2","VM","WS")) + labs(x = NULL)
p2 <- p %+% subset(pair_tabs, !SPL %in% c("AGM","BCS2","VM","WS"))
p<-arrangeGrob(grobs = lapply(
  list(p1, p2),
  set_panel_size,
  width = unit(5, "cm"),
  height = unit(4, "cm")
))
#print(p)
ggsave(device=cairo_pdf, filename, width = 9.75, height = 4.5, dpi=320,p)
rm(p,p1,p2,filename)

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
  )+facet_wrap(SPL~.,scales = "free_y", nrow = 1)
p1 <- p %+% subset(pair_tabs, SPL %in% c("AGM","BCS2","VM","WS")) + labs(x = NULL)
p2 <- p %+% subset(pair_tabs, !SPL %in% c("AGM","BCS2","VM","WS"))
p<-arrangeGrob(grobs = lapply(
  list(p1, p2),
  set_panel_size,
  width = unit(5, "cm"),
  height = unit(4, "cm")
))
#print(p)
ggsave(device=cairo_pdf, filename, width = 9.75, height = 4.5, dpi=320,p)
rm(p,p1,p2,filename)

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
  )+facet_wrap(SPL~.,scales = "free_y", nrow = 1)
p1 <- p %+% subset(pair_tabs, SPL %in% c("AGM","BCS2","VM","WS")) + labs(x = NULL)
p2 <- p %+% subset(pair_tabs, !SPL %in% c("AGM","BCS2","VM","WS"))
p<-arrangeGrob(grobs = lapply(
  list(p1, p2),
  set_panel_size,
  width = unit(5, "cm"),
  height = unit(4, "cm")
))
#print(p)
ggsave(device=cairo_pdf, filename, width = 9.75, height = 4.5, dpi=320,p)
rm(p,p1,p2,filename)

{
  corrMethod<-"pearson"
  # x_col = "RatioStates";          xlab_txt = "Ratio between total sizes of FFSM to products pair (number of states)"
  x_col = "RatioTransitions";     xlab_txt = "Ratio between total sizes of FFSM to products pair (number of transitions)"
  y_col <- "ConfigSim"; ylab_txt <- "Configuration similarity";
  # y_col <- "RatioFeatures"; ylab_txt <- "Amount of feature sharing";
  
  y_title <- "Pearson correlation coefficient - Pairwise analysis"
  filename <- paste("correlation_",x_col,"_",y_col,".pdf",sep = "")
  
  p1 <-ggscatter(subset(pair_tabs, SPL %in% c("AGM","BCS2","VM","WS")),
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
    )+ facet_wrap(SPL~.,scales = "free", nrow = 1)  + labs(x = NULL)
  # facet_wrap(SPL~.,scales = "free_x", nrow = 1)
  # facet_wrap(SPL~., nrow = 1)
  
  p2 <-ggscatter(subset(pair_tabs, !SPL %in% c("AGM","BCS2","VM","WS")),
                 x = x_col, xlab = xlab_txt,
                 y = y_col, ylab = ylab_txt,
                 title = NULL,
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
    )+ facet_wrap(SPL~.,scales = "free", nrow = 1)
  p<-arrangeGrob(grobs = lapply(
    list(p1, p2),
    set_panel_size,
    width = unit(9, "cm"),
    height = unit(5, "cm")
  ))
  #print(p)
  ggsave(device=cairo_pdf, filename, width = 17, height = 6, dpi=320,p)
  rm(p,p1,p2,filename,x_col,xlab_txt,y_col,ylab_txt,y_title,corrMethod)
}

# # Loading report files of recovering FFSMs from prioritized configurations
# # Then, combine tables in a single data structure
# prtz_tabs <- NULL
# for (logId in seq(0,99)) {
#     logId <- str_pad(logId, 2, pad = "0")
#     # for (an_spl in c("agm", "vm", "ws", "bcs2", "cpterminal", "minepump")) {
#     for (an_spl in c("agm", "vm", "ws", "bcs2", "cpterminal", "minepump", "aerouc5")) {
#       for (xmdp in c("lmdp", "gmdp", "rndp")) {
#       # for (xmdp in c("lmdp", "rndp")) {
#         # for (tsort in c("dis")) {
#         for (tsort in c("dis", "sim")) {
#           if(xmdp=="rndp" && tsort == "sim"){ next }
#           tab<-read.table(paste("./prtz/",xmdp,"_",tsort,"_",logId,"_",an_spl,"_r.txt.log",sep = ""),sep="/", header=TRUE)
#           tab$Index <- seq(nrow(tab))
#           tab$SPL <- an_spl
#           # tab$Prioritization <- paste(xmdp,tsort)
#           tab$Prioritization <- xmdp
#           tab$ID <- logId
#           tab$Criteria <- tsort
#           if(is.null(prtz_tabs)) prtz_tabs <- tab
#           else{
#             prtz_tabs <- rbind(prtz_tabs,tab)
#           }
#         }
#       }
#     }
# }
# write.table(prtz_tabs,"./recov_prtz.tab")
prtz_tabs<-read.table("./recov_prtz.tab")
# prtz_tabs<-prtz_tabs[prtz_tabs$Criteria=="dis",]
# prtz_tabs<-prtz_tabs[prtz_tabs$Prioritization!="gmdp",]

prtz_tabs$StatesOrigFFSM <- 0; prtz_tabs$TransitionsOrigFFSM <- 0 

prtz_tabs[prtz_tabs$SPL=='aerouc5',   'StatesOrigFFSM'] <- 25; prtz_tabs[prtz_tabs$SPL=='aerouc5',   'TransitionsOrigFFSM'] <- 25*18
prtz_tabs[prtz_tabs$SPL=='agm',       'StatesOrigFFSM'] <- 6;  prtz_tabs[prtz_tabs$SPL=='agm',       'TransitionsOrigFFSM'] <- 35
prtz_tabs[prtz_tabs$SPL=='bcs2',      'StatesOrigFFSM'] <- 6;  prtz_tabs[prtz_tabs$SPL=='bcs2',      'TransitionsOrigFFSM'] <- 53
prtz_tabs[prtz_tabs$SPL=='cpterminal','StatesOrigFFSM'] <- 11; prtz_tabs[prtz_tabs$SPL=='cpterminal','TransitionsOrigFFSM'] <- 11*15
prtz_tabs[prtz_tabs$SPL=='minepump',  'StatesOrigFFSM'] <- 25; prtz_tabs[prtz_tabs$SPL=='minepump',  'TransitionsOrigFFSM'] <- 25*23
prtz_tabs[prtz_tabs$SPL=='vm',        'StatesOrigFFSM'] <- 14; prtz_tabs[prtz_tabs$SPL=='vm',        'TransitionsOrigFFSM'] <- 197
prtz_tabs[prtz_tabs$SPL=='ws',        'StatesOrigFFSM'] <- 13; prtz_tabs[prtz_tabs$SPL=='ws',        'TransitionsOrigFFSM'] <- 112

# calculate model size increment in terms of the maximum size (number of states)
prtz_tabs$RatioStates<- prtz_tabs$StatesFFSM/apply(prtz_tabs[,c("TotalStatesRef","TotalStatesUpdt")],1,sum)

# calculate model size increment in terms of the maximum size (number of transitions)  
prtz_tabs$RatioTransitions<- prtz_tabs$TransitionsFFSM/apply(prtz_tabs[,c("TotalTransitionsRef","TotalTransitionsUpdt")],1,sum)

# #  
summarized_tab <- prtz_tabs %>%
  group_by(SPL,Prioritization,Criteria,ID) %>%
  summarize(
    sum_StatesFFSM = sum(StatesFFSM),
    sum_TransitionsFFSM = sum(TransitionsFFSM),
    
    sum_RatioStates      = sum(RatioStates),
    sum_RatioTransitions = sum(RatioTransitions),
    
    num_FFSMs = length(StatesFFSM),
    
    StatesOrigFFSM      = mean(StatesOrigFFSM),
    TransitionsOrigFFSM = mean(TransitionsOrigFFSM)
    )
summarized_df<-data.frame(summarized_tab)

summarized_df$AVG_Transitions <- summarized_df$sum_TransitionsFFSM/summarized_df$num_FFSMs
summarized_df$AVG_States <- summarized_df$sum_StatesFFSM/summarized_df$num_FFSMs

summarized_df$APFD_RatioTransitions <- summarized_df$sum_RatioTransitions/summarized_df$num_FFSMs
summarized_df$APFD_RatioStates      <- summarized_df$sum_RatioStates     /summarized_df$num_FFSMs

summarized_df$APFD_StatesOrigFFSM      <- summarized_df$sum_StatesFFSM/(summarized_df$num_FFSMs * summarized_df$StatesOrigFFSM)
summarized_df$APFD_TransitionsOrigFFSM <- summarized_df$sum_TransitionsFFSM/(summarized_df$num_FFSMs * summarized_df$TransitionsOrigFFSM)

summarized_df$Prioritization<- sub("gmdp","Global",summarized_df$Prioritization)
summarized_df$Prioritization<- sub("lmdp","Local",summarized_df$Prioritization)
# summarized_df$Prioritization<- sub("lmdp","Similarity",summarized_df$Prioritization)
summarized_df$Prioritization<- sub("rndp","Random",summarized_df$Prioritization)
summarized_df$SPL<-toupper(summarized_df$SPL)

# for (a_metric in c('APFD_StatesOrigFFSM', 'APFD_TransitionsOrigFFSM')) {
# for (a_metric in c('APFD_RatioTransitions', 'APFD_RatioStates')) {
for (a_metric in c('APFD_RatioTransitions', 'APFD_RatioStates','APFD_StatesOrigFFSM', 'APFD_TransitionsOrigFFSM', 'AVG_States', 'AVG_Transitio')) {
  filename <- paste(a_metric,".pdf",sep="")
  p1 <- ggplot(data=summarized_df[summarized_df$SPL %in% c("AGM","BCS2","VM","WS"),], aes_string(x="Prioritization",y=a_metric,color="Criteria",fill="Prioritization")) +
    geom_boxplot(color = "black")+
    stat_boxplot(geom ='errorbar',color = "black")+
    scale_fill_brewer(palette="Greys") +
    theme_bw() +facet_wrap(SPL~.,scales = "free", nrow = 1) + 
    labs(x = NULL)+
    theme(legend.position = "none")
  p2 <- ggplot(data=summarized_df[!summarized_df$SPL %in% c("AGM","BCS2","VM","WS"),], aes_string(x="Prioritization",y=a_metric,color="Prioritization",fill="Prioritization")) +
    geom_boxplot(color = "black")+
    stat_boxplot(geom ='errorbar',color = "black")+
    scale_fill_brewer(palette="Greys") +
    theme_bw() +facet_wrap(SPL~.,scales = "free", nrow = 1) 
  p<-arrangeGrob(grobs = lapply(
    list(p1, p2),
    set_panel_size,
    width = unit(5, "cm"),
    height = unit(4, "cm")
  ))
  #print(p)
  ggsave(device=cairo_pdf, filename, width = 9.75, height = 4.5, dpi=320,p)
  rm(p,p1,p2,filename)
  
}


lines <-c()
for (SPL in unique(summarized_df$SPL)) {
  lines<-append(lines,paste("#########",SPL))
  sub_tab<-summarized_df[(summarized_df$SPL ==SPL),]
  glo_var <- sub_tab[sub_tab$Prioritization=="Global","APFD_TransitionsOrigFFSM"]
  loc_var <- sub_tab[sub_tab$Prioritization=="Local" ,"APFD_TransitionsOrigFFSM"]
  rnd_var <- sub_tab[sub_tab$Prioritization=="Random","APFD_TransitionsOrigFFSM"]
  # global vs random
  wilc_glo<-wilcox.test(glo_var,rnd_var)
  vd_glo<-VD.A(glo_var,rnd_var)
  # local vs random
  wilc_loc<-wilcox.test(loc_var,rnd_var)
  vd_loc<-VD.A(loc_var,rnd_var)
  # global vs local
  wilc_glc<-wilcox.test(glo_var,loc_var)
  vd_glc<-VD.A(glo_var,loc_var)
  lines<-append(lines,"Global vs. Random")
  lines<-append(lines,paste("\tp-value =", wilc_glo$p.value))
  lines<-append(lines,paste("\tÂ =",       vd_glo$estimate,"(",vd_glo$magnitude,")"))
  lines<-append(lines,"Local vs. Random")
  lines<-append(lines,paste("\tp-value =", wilc_loc$p.value))
  lines<-append(lines,paste("\tÂ =",       vd_loc$estimate,"(",vd_loc$magnitude,")"))
  lines<-append(lines,"Global vs. Local")
  lines<-append(lines,paste("\tp-value =", wilc_glc$p.value))
  lines<-append(lines,paste("\tÂ =",       vd_glc$estimate,"(",vd_glc$magnitude,")"))
  lines<-append(lines,"")
}
fileConn<-file("statistics.txt")
writeLines(lines,fileConn)
close(fileConn)



for (logId in seq(0,99)) {
  logId <- str_pad(logId, 2, pad = "0")
  # for (an_spl in c("agm", "vm", "ws", "bcs2", "cpterminal", "minepump")) {
  for (an_spl in c("agm", "vm", "ws", "bcs2", "cpterminal", "minepump", "aerouc5")) {
    for (xmdp in c("lmdp", "gmdp", "rndp")) {
    # for (xmdp in c("lmdp", "rndp")) {
      # for (tsort in c("dis")) {
      for (tsort in c("dis", "sim")) {
        if(xmdp=="rndp" && tsort == "sim"){ next }
      }
    }
  }
}