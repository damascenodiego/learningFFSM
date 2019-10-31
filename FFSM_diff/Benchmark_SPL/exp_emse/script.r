list.of.packages <- c("ggpubr","ggrepel","ggplot2","effsize","stringr","reshape","dplyr")

# new.packages <- list.of.packages[!(list.of.packages %in% installed.packages(lib.loc="/home/cdnd1/Rpackages/")[,"Package"])]
# if(length(new.packages)) install.packages(new.packages,lib="/home/cdnd1/Rpackages/")
# lapply(list.of.packages,require,character.only=TRUE, lib.loc="/home/cdnd1/Rpackages/")

new.packages <- list.of.packages[!(list.of.packages %in% installed.packages()[,"Package"])]
if(length(new.packages)) install.packages(new.packages, dependencies = TRUE)
lapply(list.of.packages,require,character.only=TRUE)

rm(new.packages,list.of.packages)

# tab_lst <- NULL
# for (an_spl in c("agm", "vm", "ws", "bcs2")) {
# # for (an_spl in c("agm", "vm", "ws", "bcs2", "aerouc5", "cpterminal", "minepump")) {
#   tab_c<-read.table(paste("./pair_merging_",an_spl,".log",sep = ""),sep="/", header=TRUE)
#   tab_d<-read.table(paste("./pair_dissimilarity_",an_spl,".log",sep = ""),sep="\t", header=TRUE)
#   tab_l<-read.table(paste("./pair_comparelanguages_",an_spl,".log",sep = ""),sep="|", header=TRUE)
#   tab_m<-merge(merge(tab_c,tab_d),tab_l)
#   tab_m$SPL <- an_spl
#   if(is.null(tab_lst)){
#     tab_lst <- tab_m
#   }else{
#     tab_lst <- rbind(tab_lst,tab_m)
#   }
# }
# write.table(tab_lst,"./pair_mdc.tab")
tab_lst<-read.table("./pair_mdc.tab")

# dissimilarity histogram 
p<-ggplot(tab_lst, aes(x=ConfigDissim)) + 
  geom_histogram(color="black", fill="lightgrey", bins=7)+
  labs(x = "Configuration dissimilarity", y = "Frequency", title = "Pairwise product dissimilarity")+
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
filename <- "histogram_dissim.pdf"
ggsave(device=cairo_pdf, filename, width = 8, height = 4, dpi=320)  # ssh plots


# dissimilarity (recall) histogram 
p<-ggplot(tab_lst, aes(x=Recall)) + 
  geom_histogram(color="black", fill="lightgrey", bins=7)+
  labs(x = "Configuration dissimilarity", y = "Frequency", title = "Pairwise product dissimilarity")+
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
filename <- "histogram_recall.pdf"
ggsave(device=cairo_pdf, filename, width = 8, height = 4, dpi=320)  # ssh plots


# ratio size histogram 
p<-ggplot(tab_lst, aes(x=RatioStates)) + 
  geom_histogram(color="black", fill="lightgrey", bins=7)+
  labs(x = "Ratio betwen FFSM size to total size of products pair", y = "Frequency", title = "Ratio FFSM size to total products pair size")+
  scale_x_continuous(breaks = seq(0,1,0.1))+
  theme_bw()+
  theme(
    plot.title = element_text(hjust = 0.5, size=10),
    axis.text.x  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
    axis.text.y  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
    axis.title.x  = element_text(angle = 0,  hjust = 0.5, vjust = 0.5, size=10),
    axis.title.y  = element_text(angle = 90, hjust = 0.5, vjust = 0.5, size=10)
  )+facet_wrap(SPL~.,scales = "free_y")
# p
filename <- "histogram_ratsize.pdf"
ggsave(device=cairo_pdf, filename, width = 8, height = 4, dpi=320)  # ssh plots

tab_lst$ConfigSim <- 1-tab_lst$ConfigDissim
{
  corrMethod<-"pearson"
  # x_col = "Recall"; xlab_txt = "Recall"
  # x_col = "Precision"; xlab_txt = "Precision"
  # x_col = "F.measure"; xlab_txt = "F-Measure"
  x_col = "RatioStates"; xlab_txt = "Ratio between FFSM size to total size of products pairs"
  # y_col <- "RatioFeatures"; ylab_txt <- "Amount of feature sharing";
  y_col <- "ConfigSim"; ylab_txt <- "Configuration similarity";
  # y_col <- "ConfigDissim"; ylab_txt <- "Configuration dissimilarity"; 
  
  y_title <- "Pearson correlation coefficient"
  
  plot<-ggscatter(tab_lst,
                  x = x_col, xlab = xlab_txt,
                  y = y_col, ylab = ylab_txt,
                  title = y_title,
                  add = "reg.line",
                  cor.coeff.args = list(method = corrMethod, label.x.npc = 0.4, label.y.npc = 0.1),
                  conf.int = TRUE, # Add confidence interval
                  cor.coef = TRUE # Add correlation coefficient. see ?stat_cor
  )+
    theme_bw()+
    scale_y_continuous(breaks = seq(0,1,0.2), limits = c(0,1))+
    theme(
      plot.title = element_text(hjust = 0.5, size=10),
      axis.text.x  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
      axis.text.y  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
      axis.title.x  = element_text(angle = 0,  hjust = 0.5, vjust = 0.5, size=10),
      axis.title.y  = element_text(angle = 90, hjust = 0.5, vjust = 0.5, size=10)
    )+facet_wrap(SPL~.,scales = "free")
  
  print(plot)
  # filename <- paste("correlation_",an_spl,".pdf",sep = "")
  # filename <- "correlation.pdf"
  filename <- paste("correlation_",x_col,"_",y_col,".pdf",sep = "")
  ggsave(device=cairo_pdf, filename, width = 8, height = 4, dpi=320)  # ssh plots
}

all_tabs <- NULL
for (logId in seq(1,30)) {
    logId <- str_pad(logId, 2, pad = "0")
    for (an_spl in c("agm", "vm", "ws", "bcs2", "cpterminal", "minepump")) {
      # for (an_spl in c("agm", "vm", "ws", "bcs2", "aerouc5", "cpterminal", "minepump")) {
      for (xmdp in c("lmdp")) {
        for (tsort in c("sim", "dis")) {
          tab<-read.table(paste("../",xmdp,"_",tsort,"_",logId,"_",an_spl,"_fmeasure.log.tab",sep = ""),sep="|", header=TRUE)
          tab$Index <- seq(nrow(tab))
          tab$SPL <- an_spl
          tab$Prioritization <- xmdp
          tab$ID <- logId
          tab$Criteria <- tsort
          if(is.null(all_tabs)) all_tabs <- tab
          else{
            all_tabs <- rbind(all_tabs,tab)
          }
        }
      }
    }
}

summarized_tab <- all_tabs %>%
  group_by(SPL,Prioritization,Criteria,ID) %>%
  summarize(
    sum_Precision = sum(Precision),
    sum_Recall    = sum(Recall),
    sum_Fmeasure  = sum(F.measure),
    len_Precision = length(Precision),
    len_Recall    = length(Recall),
    len_Fmeasure  = length(F.measure),
  )
summarized_df<-data.frame(summarized_tab)

# summarized_df$APFD_Precision  <- 1-summarized_df$sum_Precision/ summarized_df$len_Precision + 1/(2*summarized_df$len_Precision)
# summarized_df$APFD_Recall     <- 1-summarized_df$sum_Recall   / summarized_df$len_Recall    + 1/(2*summarized_df$len_Recall)
# summarized_df$APFD_Fmeasure   <- 1-summarized_df$sum_Fmeasure / summarized_df$len_Fmeasure  + 1/(2*summarized_df$len_Fmeasure)

summarized_df$APFD_Precision <- summarized_df$sum_Precision/summarized_df$len_Precision
summarized_df$APFD_Recall    <- summarized_df$sum_Recall/summarized_df$len_Recall
summarized_df$APFD_Fmeasure  <- summarized_df$sum_Fmeasure/summarized_df$len_Fmeasure


for (a_metric in c('APFD_Precision', 'APFD_Recall', 'APFD_Fmeasure')) {
  plot <- ggplot(data=summarized_df, aes_string(x="SPL",y=a_metric,color="Criteria",fill="Criteria")) +
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
    scale_fill_brewer(palette="Greys") +
    theme_bw() #+ labs(x = "Software product line", y = "Number of states")
  print(plot)
  filename <- paste(a_metric,".pdf",sep="")
  ggsave(device=cairo_pdf, filename, width = 6, height = 3, dpi=320)  # ssh plots
}


# {logId<-1
# # for (logId in seq(1,30)) {
#   logId <- str_pad(logId, 2, pad = "0")
#   for (an_spl in unique(all_tabs$SPL)) {
#     sub_tab<-data.frame(all_tabs[(all_tabs$SPL==an_spl & all_tabs$ID==logId),])
#     colnames(sub_tab)
#     # tab_melt <- melt(sub_tab[,c('Index', 'Prioritization', 'Criteria',"Precision","Recall","F.measure")],  id.vars = c('Index', 'Prioritization', 'Criteria'), variable.name = 'Metric')
#     tab_melt <- melt(sub_tab[,c('Index', 'Prioritization', 'Criteria',"Recall")],  id.vars = c('Index', 'Prioritization', 'Criteria'), variable.name = 'Metric')  
#     
#     plot<-ggplot(tab_melt, aes(Index,value)) +
#       geom_line(aes(colour = Criteria))+
#       theme_bw()+
#       scale_y_continuous(breaks = seq(0,1,0.1), limits = c(0,1))+
#       theme(
#         plot.title = element_text(hjust = 0.5, size=10),
#         axis.text.x  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
#         axis.text.y  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=10),
#         axis.title.x  = element_text(angle = 0,  hjust = 0.5, vjust = 0.5, size=10),
#         axis.title.y  = element_text(angle = 90, hjust = 0.5, vjust = 0.5, size=10)
#       )+
#       labs(x = "Number of products analyzed", y="Sensitivity", title = paste('Family model recovering - Sensitivity (',an_spl,' - ID',logId,')',sep = ''))
#     print(plot)
#   }
# }
# tab_melt <- melt(all_tabs[c("Index","Prioritization","Criteria","Recall")] ,  id.vars = c('Index', 'Prioritization', 'Criteria'), variable.name = 'Metric')

# 
# ##########################################################################
# 
# tab_recov<-read.table("recovering_ffsm.tab",sep="\t", header=TRUE)
# tab_recov[,"Products.Analyzed"]<-tab_recov[,"Products.Analyzed"]*100
# plot <- ggplot(data=tab_recov, aes_string(x="Products.Analyzed", y="StatesFFSM",shape="SPL")) +
#   geom_line(stat = "identity",aes_string(linetype="SPL"))+
#   labs(
#     y = "FFSM size",
#     x = "Percentage of products analyzed",
#     title = "Size of the FFSMs recovered"
#     )+
#   scale_x_continuous(breaks = seq(0,100,10)) +
#   scale_y_continuous(breaks = seq(0,15,1))+
#   theme_bw()+
#   geom_hline(colour="gray", yintercept=6,linetype="solid") +
#   geom_hline(colour="gray", yintercept=14,linetype="dashed") +
#   geom_hline(colour="gray", yintercept=13,linetype="longdash") +
#   annotate("text",x = 90, y = 6, label="Hand-crafted AGM" , size = 3)+
#   annotate("text",x = 90, y = 14, label="Hand-crafted VM" , size = 3)+
#   annotate("text",x = 90, y = 13, label="Hand-crafted WS" , size = 3)+
#   theme(
#     # plot.title = element_text(hjust = 0.5, size=9),
#     plot.title = element_blank(),
#     legend.position="top",
#     axis.text.x  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=8),
#     axis.text.y  = element_text(angle = 0,   hjust = 0.5, vjust = 0.5, size=8),
#     axis.title.x  = element_text(angle = 0,  hjust = 0.5, vjust = 0.5, size=8),
#     axis.title.y  = element_text(angle = 90, hjust = 0.5, vjust = 0.5, size=8)
#   )
# 
# plot
# filename <- paste("recovering_ffsm_",an_spl,".pdf",sep = "")
# ggsave(device=cairo_pdf, filename, width = 6, height = 4, dpi=320)  # ssh plots
# 
# 
# 
# 
# # spl_name<-tab_recov$SPL
# spl_name<-gsub("_[0-9]+\\.[a-z]+$","",tab$Reference)
# spl_name<-toupper(gsub("^fsm_","",spl_name))
# tot_prod_siz<-tab$TotalStatesRef+tab$TotalStatesUpdt
# ffsm_siz<-tab$StatesFFSM
# tab_prods<-data.frame(SPL=spl_name,Size=ffsm_siz,Model=rep("FFSM",length(spl_name)))
# tab_prods<-rbind(tab_prods,setNames(data.frame(spl_name,tot_prod_siz,rep("All products",length(spl_name))),names(tab_prods)))
# 
# plot <- ggplot(data=tab_prods, aes_string(x="SPL",y="Size",color="Model",fill="Model")) +
#   geom_boxplot(color = "black")+
#   stat_boxplot(geom ='errorbar',color = "black")+
#   # geom_hline(colour="gray", yintercept=6,linetype="solid") +
#   # geom_hline(colour="gray", yintercept=14,linetype="dashed") +
#   # geom_hline(colour="gray", yintercept=13,linetype="longdash") +
#   # annotate("text",x = 0.650, y = 6, label="Hc AGM" , size = 2)+
#   # annotate("text",x = 1.550, y = 14, label="Hc VM" , size = 2)+
#   # annotate("text",x = 2.750, y = 13, label="Hc WS" , size = 2)+
#   # scale_y_continuous(limits=c(0,30),breaks=seq(0,30,5))+
#   scale_y_continuous(breaks=seq(0,30,5))+
#   # scale_fill_brewer(palette="Greens") +
#   scale_fill_brewer(palette="Greys") +
#   theme_bw() +
#   labs(x = "Software product line", y = "Number of states")
# plot
# filename <- "tot_size_prod.pdf"
# ggsave(device=cairo_pdf, filename, width = 6, height = 3, dpi=320)  # ssh plots
# 
# plot <- ggplot(data=tab_prods[(tab_prods$Model=="FFSM"),], aes_string(x="SPL",y="Size",color="Model",fill="SPL")) +
#   geom_boxplot(color = "black")+
#   stat_boxplot(geom ='errorbar', color = "black")+
#   geom_hline(colour="gray", yintercept=6,linetype="solid") +
#   geom_hline(colour="gray", yintercept=14,linetype="dashed") +
#   geom_hline(colour="gray", yintercept=13,linetype="longdash") +
#   annotate("text",x = 1.0, y = 6, label="Hand-crafted AGM" , size = 2.5)+
#   annotate("text",x = 2.0, y = 14, label="Hand-crafted VM" , size = 2.5)+
#   annotate("text",x = 3.0, y = 13, label="Hand-crafted WS" , size = 2.5)+
#   scale_y_continuous(limits=c(0,23),breaks=seq(0,23,2))+
#   # scale_fill_grey() +
#   scale_fill_brewer(palette="Greys") +
#   theme_bw()+
#   theme(
#     legend.position="none"Château de Chambord
#   )+
#   labs(x = "Software product line", y = "Number of states")
# 
# plot
# filename <- "tot_size_prod_2.pdf"
# ggsave(device=cairo_pdf, filename, width = 4.5, height = 3, dpi=320)  # ssh plots
# 
# 
# ffsm_orig_size<-list()
# ffsm_orig_size[["AGM"]]<-21
# ffsm_orig_size[["VM"]]<-14
# ffsm_orig_size[["WS"]]<-13
# for (variable in unique(spl_name)) {
#   tmp_tab<-tab_prods[tab_prods$SPL==variable,]
#   wilc_val<-wilcox.test(tmp_tab[tmp_tab$Model=="All products","Size"],tmp_tab[tmp_tab$Model=="FFSM","Size"])
#   print(paste(variable," p-value = ",wilc_val$p.value))
#   vd_val<-VD.A(tmp_tab$Size,tmp_tab$Model)
#   print(paste(variable," Â = ",vd_val$estimate,"(",vd_val$magnitude,")"))
# }
# 
# for (variable in unique(spl_name)) {
#   tmp_tab<-tab_prods[tab_prods$SPL==variable,]
#   subtmp_tab<-tmp_tab[tmp_tab$Model=="FFSM",]
#   subtmp_tab<-rbind(subtmp_tab,setNames(data.frame(rep(variable,length(subtmp_tab)),rep(ffsm_orig_size[[variable]],length(subtmp_tab)),"Original Size"),names(subtmp_tab)))
#   subtmp_tab$Model<-droplevels(subtmp_tab$Model)
#   wilc_val<-wilcox.test(subtmp_tab[subtmp_tab$Model=="FFSM","Size"],subtmp_tab[subtmp_tab$Model=="Original Size","Size"])
#   print(paste(variable," p-value = ",wilc_val$p.value))
#   vd_val<-VD.A(subtmp_tab$Size,subtmp_tab$Model)
#   print(paste(variable," Â = ",vd_val$estimate,"(",vd_val$magnitude,")"))
# }
# 
