list.of.packages <- c("ggplot2","reshape2","gtools","stringr","scales","effsize","SortableHTMLTables","RColorBrewer","ggpubr","nortest","cowplot")

new.packages <- list.of.packages[!(list.of.packages %in% installed.packages(lib.loc="~/Rpackages/")[,"Package"])]
if(length(new.packages)) install.packages(new.packages,lib="~/Rpackages/")
lapply(list.of.packages,require,character.only=TRUE, lib.loc="~/Rpackages/")

# new.packages <- list.of.packages[!(list.of.packages %in% installed.packages()[,"Package"])]
# if(length(new.packages)) install.packages(new.packages, dependencies = TRUE)
# lapply(list.of.packages,require,character.only=TRUE)

rm(list.of.packages,new.packages)

# Multiple plot function
#
# ggplot objects can be passed in ..., or to plotlist (as a list of ggplot objects)
# - cols:   Number of columns in layout
# - layout: A matrix specifying the layout. If present, 'cols' is ignored.
#
# If the layout is something like matrix(c(1,2,3,3), nrow=2, byrow=TRUE),
# then plot 1 will go in the upper left, 2 will go in the upper right, and
# 3 will go all the way across the bottom.
#
multiplot <- function(..., plotlist=NULL, file, cols=1, layout=NULL) {
  library(grid)
  
  # Make a list from the ... arguments and plotlist
  plots <- c(list(...), plotlist)
  
  numPlots = length(plots)
  
  # If layout is NULL, then use 'cols' to determine layout
  if (is.null(layout)) {
    # Make the panel
    # ncol: Number of columns of plots
    # nrow: Number of rows needed, calculated from # of cols
    layout <- matrix(seq(1, cols * ceiling(numPlots/cols)),
                     ncol = cols, nrow = ceiling(numPlots/cols))
  }
  
  if (numPlots==1) {
    print(plots[[1]])
    
  } else {
    # Set up the page
    grid.newpage()
    pushViewport(viewport(layout = grid.layout(nrow(layout), ncol(layout))))
    
    # Make each plot, in the correct location
    for (i in 1:numPlots) {
      # Get the i,j matrix positions of the regions that contain this subplot
      matchidx <- as.data.frame(which(layout == i, arr.ind = TRUE))
      
      print(plots[[i]], vp = viewport(layout.pos.row = matchidx$row,
                                      layout.pos.col = matchidx$col))
    }
  }
}

## Gives count, mean, standard deviation, standard error of the mean, and confidence interval (default 95%).
##   data: a data frame.
##   measurevar: the name of a column that contains the variable to be summariezed
##   groupvars: a vector containing names of columns that contain grouping variables
##   na.rm: a boolean that indicates whether to ignore NA's
##   conf.interval: the percent range of the confidence interval (default is 95%)
summarySE <- function(data=NULL, measurevar, groupvars=NULL, na.rm=FALSE,
                      conf.interval=.95, .drop=TRUE) {
  library(plyr)
  
  # New version of length which can handle NA's: if na.rm==T, don't count them
  length2 <- function (x, na.rm=FALSE) {
    if (na.rm) sum(!is.na(x))
    else       length(x)
  }
  
  # This does the summary. For each group's data frame, return a vector with
  # N, mean, and sd
  datac <- ddply(data, groupvars, .drop=.drop,
                 .fun = function(xx, col) {
                   c(N    = length2(xx[[col]], na.rm=na.rm),
                     mean = mean   (xx[[col]], na.rm=na.rm),
                     sd   = sd     (xx[[col]], na.rm=na.rm)
                   )
                 },
                 measurevar
  )
  
  # Rename the "mean" column    
  datac <- rename(datac, c("mean" = measurevar))
  
  datac$se <- datac$sd / sqrt(datac$N)  # Calculate standard error of the mean
  
  # Confidence interval multiplier for standard error
  # Calculate t-statistic for confidence interval: 
  # e.g., if conf.interval is .95, use .975 (above/below), and use df=N-1
  ciMult <- qt(conf.interval/2 + .5, datac$N-1)
  datac$ci <- datac$se * ciMult
  
  return(datac)
}

loadTabAsDataFrame <-function(filename){
  data <- read.table(tab_filename, sep="\t", header=TRUE)
  
  # reused_lst  <- levels(data$Reused)
  # reused_lst  <-reused_lst [! (reused_lst %in% c('N/A'))]
  # reused_lst  <- c(c("N/A"),reused_lst)
  # data$Reused <- factor(data$Reused, reused_lst)
  
  # data$SUL<-as.character(data$SUL)
  # data$Reused<-as.character(data$Reused)
  data$Rounds<-as.numeric(data$Rounds)
  data$MQ_Resets<-as.numeric(data$MQ_Resets)
  data$MQ_Symbols<-as.numeric(data$MQ_Symbols)
  data$EQ_Resets<-as.numeric(data$EQ_Resets)
  data$EQ_Symbols<-as.numeric(data$EQ_Symbols)
  data$LearningTime<-as.numeric(data$LearningTime)
  data$CEX_SearchTime<-as.numeric(data$CEX_SearchTime)
  # data$Equivalent<-as.character(data$Equivalent)
  
  data$TQ_Resets<-data$EQ_Resets+data$MQ_Resets
  data$TQ_Symbols<-data$EQ_Symbols+data$MQ_Symbols
  
  
  return(data)
}

cleanTab<-function(the_tab){
  the_tab$Method<-gsub("[sS]tar","*M",the_tab$Method)
  # the_tab$Method<-gsub("Adaptive","Adpt",the_tab$Method)
  the_tab$Method<-gsub("AdaptiveL\\*M","Adapt",the_tab$Method)
  the_tab$Method<-gsub("_"," ",the_tab$Method)
  the_tab$SUL<-gsub("\\.dot$","",gsub("\\.fixed$","",gsub("\\.txt$","",the_tab$SUL)))
  the_tab$Reused<-gsub("\\.dot$","",gsub("\\.fixed$","",gsub("\\.txt$","",the_tab$Reused)))
  # the_tab$Method<-factor(the_tab$Method,levels = c("DL*M v2","DL*M v1","AdptL*M","L1","L*M"))
  the_tab$Method<-factor(the_tab$Method,levels = c("L*M","L1","Adapt","DL*M v1","DL*M v2"))
  return(the_tab);
}

cleanSulReusedCols<-function(the_tab,the_find,the_repl){
  the_tab$Reused<-gsub(the_find,the_repl,the_tab$Reused)
  the_tab$SUL<-gsub(the_find,the_repl,the_tab$SUL)
  return(the_tab)
}

plotsOnlyReusingV0 <- function(tmp_summ,outDir, the_wid = 15, the_hei = 4, the_dpi=320, the_nrow = 2){
  uniq_sul<-unique(tmp_summ[(!(tmp_summ$Reused=="-")),]$SUL)
  uniq_ruz<-unique(tmp_summ$Reused); uniq_ruz<-uniq_ruz[uniq_ruz!="-"]
  bplots<-list()
  for(sul in uniq_sul){
    reused<-match(sul,uniq_sul)-1; if(reused==0) next; 
    reused<-uniq_sul[[1]]
    tmp_local<-tmp_summ[((tmp_summ$SUL==sul) & ((tmp_summ$Reused==reused) | (tmp_summ$Reused=="-"))),]
    p2 <- ggplot(data=tmp_local, aes_string(x="Method", y=metric_id)) +
        geom_boxplot(outlier.shape=NA) +
        theme(plot.title = element_text(hjust = 0.5,size=7),
              legend.position="none",
              axis.text.x = element_text(angle = 45, hjust = 1,size=7),
              axis.text.y = element_text(angle = 45, hjust = 1,size=7),
              axis.title.x = element_blank(),
              axis.title.y = element_blank()
        ) +
        labs(title = paste(sul,reused,sep = " w/"))+
      coord_cartesian(ylim=c(
        # min(tmp_summ[,metric_id]-tmp_summ[,"sd"]),
        # max(tmp_summ[,metric_id]+tmp_summ[,"sd"])))
        # min(tmp_summ[,metric_id])-min(tmp_summ[,"sd"]),
        # max(tmp_summ[,metric_id])+min(tmp_summ[,"sd"])))
        min(tmp_summ[(tmp_summ$SUL==sul),metric_id])-min(tmp_summ[(tmp_summ$SUL==sul),"sd"]),
        max(tmp_summ[(tmp_summ$SUL==sul),metric_id])+min(tmp_summ[(tmp_summ$SUL==sul),"sd"])))

    bplots[[sul]]<-p2
  }
  p2<-plot_grid(plotlist=bplots,nrow = 2)
  filename <- paste(outDir,"boxplot_v0_",metric_id,"_",sul,".png",sep="");
  ggsave(filename, width = the_wid, height = the_hei,dpi=the_dpi)
  # ggsave(filename, width = 15, height = 4,dpi=320)
}

plotsOnlyReusingPrev <- function(tmp_summ,outDir, the_wid = 15, the_hei = 4, the_dpi=320, the_nrow = 2){
  # write.table(the_summary,filename,sep="\t",row.names=FALSE, quote=FALSE,dec=",",append=FALSE)
  uniq_sul<-unique(tmp_summ[(!(tmp_summ$Reused=="-")),]$SUL)
  uniq_ruz<-unique(tmp_summ$Reused); uniq_ruz<-uniq_ruz[uniq_ruz!="-"]
  bplots<-list()
  for(sul in uniq_sul){
    reused<-match(sul,uniq_sul)-1; if(reused==0) next; 
    reused<-uniq_sul[[as.integer(reused)]]
    tmp_local<-tmp_summ[((tmp_summ$SUL==sul) & ((tmp_summ$Reused==reused) | (tmp_summ$Reused=="-"))),]
    p2 <- ggplot(data=tmp_local, aes_string(x="Method", y=metric_id)) +
      geom_boxplot(outlier.shape=NA) +
      theme(plot.title = element_text(hjust = 0.5,size=7),
            legend.position="none",
            axis.text.x = element_text(angle = 45, hjust = 1,size=7),
            axis.text.y = element_text(angle = 45, hjust = 1,size=7),
            axis.title.x = element_blank(),
            axis.title.y = element_blank()
      ) +
      labs(title = paste(sul,reused,sep = " w/"))+
      coord_cartesian(ylim=c(
        # min(tmp_summ[,metric_id]-tmp_summ[,"sd"]),
        # max(tmp_summ[,metric_id]+tmp_summ[,"sd"])))
        # min(tmp_summ[,metric_id])-min(tmp_summ[,"sd"]),
        # max(tmp_summ[,metric_id])+min(tmp_summ[,"sd"])))
        min(tmp_summ[(tmp_summ$SUL==sul),metric_id])-min(tmp_summ[(tmp_summ$SUL==sul),"sd"]),
        max(tmp_summ[(tmp_summ$SUL==sul),metric_id])+min(tmp_summ[(tmp_summ$SUL==sul),"sd"])))
    bplots[[sul]]<-p2
  }
  p2<-plot_grid(plotlist=bplots,nrow = 2)
  filename <- paste(outDir,"boxplot_vprev_",metric_id,"_",sul,".png",sep="");
  ggsave(filename, width = the_wid, height = the_hei,dpi=the_dpi)
  # ggsave(filename, width = 15, height = 4,dpi=320)
}

plotsReusingCustom <- function(tmp_summ,listOfPairs,outDir, the_wid = 15, the_hei = 4, the_dpi=320, the_nrow = 2){
  # write.table(the_summary,filename,sep="\t",row.names=FALSE, quote=FALSE,dec=",",append=FALSE)
  uniq_sul<-unique(tmp_summ[(!(tmp_summ$Reused=="-")),]$SUL)
  uniq_ruz<-unique(tmp_summ$Reused); uniq_ruz<-uniq_ruz[uniq_ruz!="-"]
  bplots<-list()
  for(pair_num in seq(nrow(listOfPairs))){
    sul<-listOfPairs[pair_num,1]
    reused<-listOfPairs[pair_num,2]
    tmp_local<-tmp_summ[((tmp_summ$SUL==sul) & ((tmp_summ$Reused==reused) | (tmp_summ$Reused=="-"))),]
    p2 <- ggplot(data=tmp_local, aes_string(x="Method", y=metric_id)) +
      geom_boxplot(outlier.shape=NA) +
      theme(plot.title = element_text(hjust = 0.5,size=7),
            legend.position="none",
            axis.text.x = element_text(angle = 45, hjust = 1,size=7),
            axis.text.y = element_text(angle = 45, hjust = 1,size=7),
            axis.title.x = element_blank(),
            axis.title.y = element_blank()
      ) +
      labs(title = paste(sul,reused,sep = " w/"))#+
    # coord_cartesian(ylim=c(
    #   min(tmp_summ[(tmp_summ$SUL==sul),metric_id]-tmp_summ[(tmp_summ$SUL==sul),"sd"]), 
    #   max(tmp_summ[(tmp_summ$SUL==sul),metric_id]+tmp_summ[(tmp_summ$SUL==sul),"sd"])))
    # filename <- paste(plotdir,"/","boxplot_",metric_id,"_",sul,"_",reused,"_",fname,".png",sep="");
    # ggsave(filename, dpi=320,title=paste(metric_id,"_boxplot_",fname,sep = ""))
    bplots[[pair_num]]<-p2
  }
  p2<-plot_grid(plotlist=bplots,nrow = 2)
  filename <- paste(outDir,"boxplot_custom_",metric_id,"_",sul,".png",sep="");
  ggsave(filename, width = the_wid, height = the_hei,dpi=the_dpi)
  # ggsave(filename, width = 15, height = 4,dpi=320)
  # ggsave(filename,dpi=640)
}


plotsAllPairs <- function(tmp_summ,outDir, the_wid = 15, the_hei = 4, the_dpi=320, the_nrow = 2){
  # write.table(the_summary,filename,sep="\t",row.names=FALSE, quote=FALSE,dec=",",append=FALSE)
  uniq_sul<-unique(tmp_summ[(!(tmp_summ$Reused=="-")),]$SUL)
  uniq_ruz<-unique(tmp_summ$Reused); uniq_ruz<-uniq_ruz[uniq_ruz!="-"]
  bplots<-list()
  plot_idx<-1
  for(sul in uniq_sul){
    for(reused in uniq_ruz){
      if(sul==reused) next;
      tmp_local<-tmp_summ[((tmp_summ$SUL==sul) & ((tmp_summ$Reused==reused) | (tmp_summ$Reused=="-"))),]
      p2 <- ggplot(data=tmp_local, aes_string(x="Method", y=metric_id)) +
        geom_boxplot(outlier.shape=NA) +
        theme(plot.title = element_text(hjust = 0.5,size=7),
              legend.position="none",
              axis.text.x = element_text(angle = 45, hjust = 1,size=7),
              axis.text.y = element_text(angle = 45, hjust = 1,size=7),
              axis.title.x = element_blank(),
              axis.title.y = element_blank()
        ) +
        labs(title = paste(sul,reused,sep = " w/"))
      # +coord_cartesian(ylim=c(
      #   # min(tmp_summ[,metric_id]-tmp_summ[,"sd"]),
      #   # max(tmp_summ[,metric_id]+tmp_summ[,"sd"])))
      #   # min(tmp_summ[,metric_id])-min(tmp_summ[,"sd"]),
      #   # max(tmp_summ[,metric_id])+min(tmp_summ[,"sd"])))
      #   min(tmp_summ[(tmp_summ$SUL==sul),metric_id]-tmp_summ[(tmp_summ$SUL==sul),"sd"]),
      #   max(tmp_summ[(tmp_summ$SUL==sul),metric_id]+tmp_summ[(tmp_summ$SUL==sul),"sd"])))
      # filename <- paste(plotdir,"/","boxplot_",metric_id,"_",sul,"_",reused,"_",fname,".png",sep="");
      # ggsave(filename, dpi=320,title=paste(metric_id,"_boxplot_",fname,sep = ""))
      bplots[[plot_idx]]<-p2
      plot_idx<-plot_idx+1
    }
    # # bplots[[names(bplots)[1]]] <- bplots[[names(bplots)[1]]] + theme(axis.title.y = element_text(angle = 90,size=7))
    # # bplots[[names(bplots)[10]]] <- bplots[[names(bplots)[10]]] + theme(axis.title.y = element_text(angle = 90,size=7))
  }
  p2<-plot_grid(plotlist=bplots,nrow = the_nrow)
  filename <- paste(outDir,"boxplot_allpairs_",sul,"_",metric_id,".png",sep="");
  ggsave(filename, width = the_wid, height = the_hei,dpi=the_dpi)
  # ggsave(filename,dpi=640)
}
logdir<-"/home/cdnd1/eclipse-workspace/FM_Analysis/"
fname<-"uniqLog_logback_2019_01_04_09_45_"
tab_filename<-paste(logdir,fname,".tab",sep="")

data <- loadTabAsDataFrame(tab_filename)

# for(metric_id in c("MQ_Resets","EQ_Resets","TQ_Resets","Rounds"))
for(metric_id in c("MQ_Resets","EQ_Resets","TQ_Resets"))
# metric_id<-"TQ_Resets"
# metric_id<-"Rounds"
{
  summary<-summarySE(data, measurevar=metric_id, groupvars=c("Method", "SUL", "Reused", "Equivalent"))
  summary<-cleanTab(summary)
  summary<-summary[(summary$Method!="L1"),]
  # summary<-summary[(summary$Method!="DL*M v1"),]
  theOutDir<-"./"
  
  # pattern<-paste("(^client_)",sep="|")
  # summary_ssl_cli<-summary[grepl(pattern,summary$SUL),]
  # summary_ssl_cli<-cleanSulReusedCols(summary_ssl_cli,"^client_","cli_")
  # plotsOnlyReusingV0(summary_ssl_cli,theOutDir)
  # plotsOnlyReusingPrev(summary_ssl_cli,theOutDir)

  pattern<-paste("(^server_)",sep="|")
  summary_ssl_srv<-summary[grepl(pattern,summary$SUL),]
  summary_ssl_srv<-cleanSulReusedCols(summary_ssl_srv,"^server_","srv_")
  plotsOnlyReusingV0(summary_ssl_srv,theOutDir,the_wid = 10,the_hei = 3,the_nrow = 3,the_dpi = 1000)
  # plotsOnlyReusingPrev(summary_ssl_srv,theOutDir)
  
  # lpairs<-matrix(c(_
  #   "srv_097c","srv_097e","srv_098l","srv_098m","srv_100","srv_098s","srv_098u","srv_101","srv_098za","srv_101k","srv_102","srv_110pre1",
  #   "srv_097","srv_097c","srv_097e","srv_098l","srv_098m","srv_100","srv_098s","srv_098u","srv_101","srv_098za","srv_101k","srv_102")
  #   ,ncol=2)
  # plotsReusingCustom(summary_ssl_srv,lpairs,theOutDir)
  
  # pattern<-paste("(^QUICprotocolwith0RTT)","(^QUICprotocolwithout0RTT)",sep="|")
  # summary_quic<-summary[grepl(pattern,summary$SUL),]
  # summary_quic<-cleanSulReusedCols(summary_quic,"protocolwithout0RTT","")
  # summary_quic<-cleanSulReusedCols(summary_quic,"protocolwith","_")
  # plotsAllPairs(summary_quic,theOutDir,the_wid = 6,the_hei = 3,the_nrow = 1,the_dpi = 1000)
  
  pattern<-paste("(^BitVise)","(^DropBear)","(^OpenSSH)",sep="|")
  summary_ssh<-summary[grepl(pattern,summary$SUL),]
  plotsAllPairs(summary_ssh,theOutDir,the_wid = 4,the_hei = 6,the_nrow = 3,the_dpi = 1000)
  
  # pattern<-paste("(^TCP_FreeBSD_Client)","(^TCP_Linux_Client)","(^TCP_Windows8_Client)",sep="|")
  # summary_tcp_cli<-summary[grepl(pattern,summary$SUL),]
  # summary_tcp_cli<-cleanSulReusedCols(summary_tcp_cli,"TCP_","")
  # summary_tcp_cli<-cleanSulReusedCols(summary_tcp_cli,"Client$","cli")
  # summary_tcp_cli<-cleanSulReusedCols(summary_tcp_cli,"Windows8 _cli","Win8")
  # summary_tcp_cli<-cleanSulReusedCols(summary_tcp_cli,"Linux_cli","Lnx")
  # summary_tcp_cli<-cleanSulReusedCols(summary_tcp_cli,"FreeBSD_cli","BSD")
  # plotsAllPairs(summary_tcp_cli,theOutDir,the_wid = 4,the_hei = 6,the_nrow = 3,the_dpi = 1000)

  pattern<-paste("(^TCP_FreeBSD_Server)","(^TCP_Linux_Server)","(^TCP_Windows8_Server)",sep="|")
  summary_tcp_srv<-summary[grepl(pattern,summary$SUL),]
  summary_tcp_srv<-summary[grepl(pattern,summary$SUL),]
  summary_tcp_srv<-cleanSulReusedCols(summary_tcp_srv,"TCP_","")
  summary_tcp_srv<-cleanSulReusedCols(summary_tcp_srv,"Server$","srv")
  summary_tcp_srv<-cleanSulReusedCols(summary_tcp_srv,"Windows8_srv","Win8")
  # summary_tcp_srv<-cleanSulReusedCols(summary_tcp_srv,"Linux_srv","Lnx")
  summary_tcp_srv<-cleanSulReusedCols(summary_tcp_srv,"FreeBSD_srv","BSD")
  plotsAllPairs(summary_tcp_srv,theOutDir,the_wid = 4,the_hei = 6,the_nrow = 3,the_dpi = 1000)
  
  rm(pattern,metric_id,theOutDir)
}
