require(ggplot2)

# 1) read the tab file
df <- read.table(file = 'exec_time.tab', header=TRUE)

# 2) Calculate the max size of the product pair
df$States <- apply(df[,c("TotalStatesRef","TotalStatesUpdt")],1,sum,na.rm=TRUE)

# increment<-25
# for(i in seq(increment,max(df$States),increment)){
#   df[df[,"States"] %in% seq(i-increment,i),"States"] <-i-increment
# }

# 3) Call the pdf command to start the plot
pdf(file = "exec_time.pdf",   # The directory you want to save the file in
    width = 12, # The width of the plot in inches
    height = 6) # The height of the plot in inches

# 4) Create the plot with R code
boxplot(Time~States,data=df, xlab="Total number of state pairs", ylab="Time taken by FFSM_Diff (in ms)", col="gray")

# 5) Save the plot to a pdf
dev.off()
