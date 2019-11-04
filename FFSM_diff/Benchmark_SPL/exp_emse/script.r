list.of.packages <- c("ggpubr","ggrepel","ggplot2","effsize","stringr","reshape","dplyr")

# new.packages <- list.of.packages[!(list.of.packages %in% installed.packages(lib.loc="/home/cdnd1/Rpackages/")[,"Package"])]
# if(length(new.packages)) install.packages(new.packages,lib="/home/cdnd1/Rpackages/")
# lapply(list.of.packages,require,character.only=TRUE, lib.loc="/home/cdnd1/Rpackages/")

new.packages <- list.of.packages[!(list.of.packages %in% installed.packages()[,"Package"])]
if(length(new.packages)) install.packages(new.packages, dependencies = TRUE)
lapply(list.of.packages,require,character.only=TRUE)

rm(new.packages,list.of.packages)

# all_tabs <- NULL
# # for (an_spl in c("agm", "bcs2")) {
# # for (an_spl in c("agm", "vm", "ws", "bcs2")) {
# for (an_spl in c("agm", "vm", "ws", "bcs2", "cpterminal", "minepump")) {
# # for (an_spl in c("agm", "vm", "ws", "bcs2", "cpterminal", "minepump", "aerouc5")) {
#   tab_c<-read.table(paste("./pair/pair_merging_",an_spl,".log",sep = ""),sep="/", header=TRUE)
#   tab_d<-read.table(paste("./pair/pair_dissimilarity_",an_spl,".log",sep = ""),sep="\t", header=TRUE)
#   tab_l<-read.table(paste("./pair/pair_comparelanguages_",an_spl,".log",sep = ""),sep="|", header=TRUE)
#   tab_m<-merge(merge(tab_c,tab_d),tab_l)
#   tab_m$SPL <- an_spl
#   if(is.null(all_tabs)){
#     all_tabs <- tab_m
#   }else{
#     all_tabs <- rbind(all_tabs,tab_m)
#   }
#   rm(tab_c,tab_d,tab_l,tab_m)
# }
# write.table(all_tabs,"./pair_mdc.tab")
all_tabs<-read.table("./pair_mdc.tab")

# calculate similarity (opposite of dissim)
all_tabs$ConfigSim <- 1-all_tabs$ConfigDissim

# calculate model size increment in terms of the maximum size (number of states)
all_tabs$RatioStates<- apply(all_tabs[,c("TotalStatesRef","TotalStatesUpdt")],1,sum)
all_tabs$RatioStates<- all_tabs$StatesFFSM/all_tabs$RatioStates

# calculate model size increment in terms of the maximum size (number of transitions)  
all_tabs$RatioTransitions<- apply(all_tabs[,c("TotalTransitionsRef","TotalTransitionsUpdt")],1,sum)
all_tabs$RatioTransitions<- all_tabs$TransitionsFFSM/all_tabs$RatioTransitions

# # calculate model size increment in terms of the largest model size (number of states)
# all_tabs$RatioStatesMax<- apply(all_tabs[,c("TotalStatesRef","TotalStatesUpdt")],1,max)
# all_tabs$RatioStatesMax<- all_tabs$StatesFFSM/all_tabs$RatioStatesMax
# 
# # calculate model size increment in terms of the largest model size (number of transitions)  
# all_tabs$RatioTransitionsMax<- apply(all_tabs[,c("TotalTransitionsRef","TotalTransitionsUpdt")],1,max)
# all_tabs$RatioTransitionsMax<- all_tabs$TransitionsFFSM/all_tabs$RatioTransitionsMax

# dissimilarity histogram 
p<-ggplot(all_tabs, aes(x=ConfigSim)) + 
  geom_histogram(color="black", fill="lightgrey", bins=7)+
  labs(x = "Configuration similarity", y = "Frequency", title = "Pairwise product similarity")+
  scale_x_continuous(breaks = seq(0,1,0.1))+
  theme_bw()+
  theme(
    plot.title = element_text(hjust = 0.5, size=10),
    axis.text.x  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
    axis.text.y  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
    axis.title.x  = element_text(angle = 0,  hjust = 0.5, vjust = 0.5, size=10),
    axis.title.y  = element_text(angle = 90, hjust = 0.5, vjust = 0.5, size=10)
  )+facet_wrap(SPL~.,scales = "free_y")
print(p)
filename <- "histogram_ConfigSim.pdf"
ggsave(device=cairo_pdf, filename, width = 8, height = 4, dpi=320)
rm(p,filename)

# dissimilarity (RatioStates) histogram
p<-ggplot(all_tabs, aes(x=RatioStates)) +
  geom_histogram(color="black", fill="lightgrey", bins=7)+
  labs(x = "Pairwise products ratio size (num. of states)", y = "Frequency", title = "Ratio between FFSM size to total size of products pairs (number of states)")+
  # scale_x_continuous(breaks = seq(0,1,0.1))+
  theme_bw()+
  theme(
    plot.title = element_text(hjust = 0.5, size=10),
    axis.text.x  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
    axis.text.y  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
    axis.title.x  = element_text(angle = 0,  hjust = 0.5, vjust = 0.5, size=10),
    axis.title.y  = element_text(angle = 90, hjust = 0.5, vjust = 0.5, size=10)
  )+facet_wrap(SPL~.,scales = "free_y")
print(p)
filename <- "histogram_RatioStates.pdf"
ggsave(device=cairo_pdf, filename, width = 8, height = 4, dpi=320)
rm(p,filename)

# dissimilarity (RatioTransitions) histogram
p<-ggplot(all_tabs, aes(x=RatioTransitions)) +
  geom_histogram(color="black", fill="lightgrey", bins=7)+
  labs(x = "Pairwise products ratio size (num. of transitions)", y = "Frequency", title = "Ratio between FFSM size to total size of products pairs (number of transitions)")+
  # scale_x_continuous(breaks = seq(0,1,0.1))+
  theme_bw()+
  theme(
    plot.title = element_text(hjust = 0.5, size=10),
    axis.text.x  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
    axis.text.y  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
    axis.title.x  = element_text(angle = 0,  hjust = 0.5, vjust = 0.5, size=10),
    axis.title.y  = element_text(angle = 90, hjust = 0.5, vjust = 0.5, size=10)
  )+facet_wrap(SPL~.,scales = "free_y")
print(p)
filename <- "histogram_RatioTransitions.pdf"
ggsave(device=cairo_pdf, filename, width = 8, height = 4, dpi=320)
rm(p,filename)

{
  corrMethod<-"pearson"
  x_col = "RatioStates";          xlab_txt = "Ratio between total sizes of FFSM to products pair (number of states)"
  x_col = "RatioTransitions";     xlab_txt = "Ratio between total sizes of FFSM to products pair (number of transitions)"
  # y_col <- "RatioFeatures"; ylab_txt <- "Amount of feature sharing";
  y_col <- "ConfigSim"; ylab_txt <- "Configuration similarity";
  
  y_title <- "Pearson correlation coefficient - Pairwise analysis"
  
  p<-ggscatter(all_tabs,
                  x = x_col, xlab = xlab_txt,
                  y = y_col, ylab = ylab_txt,
                  title = y_title,
                  add = "reg.line",
                  cor.coeff.args = list(method = corrMethod, label.x.npc = 0.4, label.y.npc = 0.1),
                  conf.int = TRUE, # Add confidence interval
                  cor.coef = TRUE # Add correlation coefficient. see ?stat_cor
  )+
    theme_bw()+
    # scale_y_continuous(breaks = seq(0,1,0.2), limits = c(0,1))+
    # scale_x_continuous(breaks = seq(0.5,1,0.1), limits = c(0.5,1))+
    theme(
      plot.title = element_text(hjust = 0.5, size=10),
      axis.text.x  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
      axis.text.y  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
      axis.title.x  = element_text(angle = 0,  hjust = 0.5, vjust = 0.5, size=10),
      axis.title.y  = element_text(angle = 90, hjust = 0.5, vjust = 0.5, size=10)
    )+facet_wrap(SPL~.,scales = "free")
  
  print(p)
  filename <- paste("correlation_",x_col,"_",y_col,".pdf",sep = "")
  ggsave(device=cairo_pdf, filename, width = 8, height = 4, dpi=320)
  rm(p,filename,x_col,xlab_txt,y_col,ylab_txt,y_title,corrMethod)
}

# all_tabs <- NULL
# for (logId in seq(0,29)) {
#     logId <- str_pad(logId, 2, pad = "0")
#     for (an_spl in c("agm", "vm", "ws", "bcs2", "cpterminal", "minepump")) {
#       # for (an_spl in c("agm", "vm", "ws", "bcs2", "cpterminal", "minepump", "aerouc5")) {
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
#           if(is.null(all_tabs)) all_tabs <- tab
#           else{
#             all_tabs <- rbind(all_tabs,tab)
#           }
#         }
#       }
#     }
# }
# write.table(all_tabs,"./recov_prtz.tab")
all_tabs<-read.table("./recov_prtz.tab")
all_tabs<-all_tabs[all_tabs$Criteria=="dis",]

# calculate model size increment in terms of the maximum size (number of states)
all_tabs$RatioStates<- apply(all_tabs[,c("TotalStatesRef","TotalStatesUpdt")],1,sum)
all_tabs$RatioStates<- all_tabs$StatesFFSM/all_tabs$RatioStates

# calculate model size increment in terms of the maximum size (number of transitions)  
all_tabs$RatioTransitions<- apply(all_tabs[,c("TotalTransitionsRef","TotalTransitionsUpdt")],1,sum)
all_tabs$RatioTransitions<- all_tabs$TransitionsFFSM/all_tabs$RatioTransitions

# calculate model size increment in terms of the largest model size (number of states)
all_tabs$RatioStatesMax<- apply(all_tabs[,c("TotalStatesRef","TotalStatesUpdt")],1,max)
all_tabs$RatioStatesMax<- all_tabs$StatesFFSM/all_tabs$RatioStatesMax

# calculate model size increment in terms of the largest model size (number of transitions)
all_tabs$RatioTransitionsMax<- apply(all_tabs[,c("TotalTransitionsRef","TotalTransitionsUpdt")],1,max)
all_tabs$RatioTransitionsMax<- all_tabs$TransitionsFFSM/all_tabs$RatioTransitionsMax

summarized_tab <- all_tabs %>%
  group_by(SPL,Prioritization,Criteria,ID) %>%
  summarize(
    sum_RatioTransitions = sum(RatioTransitions),
    len_RatioTransitions = length(RatioTransitions),
    sum_RatioStates      = sum(RatioStates),
    len_RatioStates      = length(RatioStates),
    sum_RatioTransitionsMax = sum(RatioTransitionsMax),
    len_RatioTransitionsMax = length(RatioTransitionsMax),
    sum_RatioStatesMax      = sum(RatioStatesMax),
    len_RatioStatesMax      = length(RatioStatesMax),
  )
summarized_df<-data.frame(summarized_tab)

summarized_df$APFD_RatioTransitions <- summarized_df$sum_RatioTransitions/summarized_df$len_RatioTransitions
summarized_df$APFD_RatioStates      <- summarized_df$sum_RatioStates     /summarized_df$len_RatioStates
summarized_df$APFD_RatioTransitionsMax <- summarized_df$sum_RatioTransitionsMax/summarized_df$len_RatioTransitionsMax
summarized_df$APFD_RatioStatesMax      <- summarized_df$sum_RatioStatesMax     /summarized_df$len_RatioStatesMax


for (a_metric in c('APFD_RatioTransitions', 'APFD_RatioStates')) {
# for (a_metric in c('APFD_RatioTransitions', 'APFD_RatioStates','APFD_RatioTransitionsMax', 'APFD_RatioStatesMax')) {
  p <- ggplot(data=summarized_df[(summarized_df$Criteria!="sim"),], aes_string(x="Prioritization",y=a_metric,color="Prioritization",fill="Prioritization")) +
    geom_boxplot(color = "black")+
    stat_boxplot(geom ='errorbar',color = "black")+
    # geom_hline(colour="gray", yintercept=6,linetype="solid") +
    # geom_hline(colour="gray", yintercept=14,linetype="dashed") +
    # geom_hline(colour="gray", yintercept=13,linetype="longdash") +
    # annotate("text",x = 0.650, y = 6, label="Hc AGM" , size = 2)+
    # annotate("text",x = 1.550, y = 14, label="Hc VM" , size = 2)+
    # annotate("text",x = 2.750, y = 13, label="Hc WS" , size = 2)+
    # scale_y_continuous(limits=c(0,1),breaks=seq(0,1,0.1))+
    # scale_fill_brewer(palette="Greens") +
    # labs(x = "Software product line", y = "Number of states")+
    scale_fill_brewer(palette="Greys") +
    theme_bw() +facet_wrap(SPL~.,scales = "free")
  print(p)
  filename <- paste(a_metric,".pdf",sep="")
  ggsave(device=cairo_pdf, filename, width = 6, height = 3, dpi=320)  # ssh plots
  rm(p,filename,a_metric)
}
