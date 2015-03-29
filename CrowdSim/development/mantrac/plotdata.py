#!/usr/bin/python 
import mantrac
import pylab
import re
import Tkinter as tk
import traceback
import sys
import tkMessageBox
import os

"""
This GUI program request path to a data file. It also request information related to processing the file.
The file must be of the following format.

Each line
Frame No (0-), Grid number (0-), value x, value y


Furthermore, the values gridx and gridy entered at the gui should be such that 
all grid numbers are < gridx * gridy

Known bugs: In windows, once the program is used to plot data, it bahaves slowly (but the final result is the same) 
if the same instance is used to plot another graph. Recommended work around is to close the session and start again.
"""

def readData(videoCSVFile,expr):
	#print "processing file"+file
	conData = [None] * (expr.grid_width * expr.grid_height)
	frameData = []
	f = open(videoCSVFile,'r')
	frameCount = 0
	lastFrame = -1
	for line in f:
		words = re.split(",\s*|\s+",line[:-1])
		cellNo_temp = int(words[1])
		gx = cellNo_temp % expr.grid_width
		gy = cellNo_temp / expr.grid_width
		cellNo = gx + (expr.grid_height - gy - 1) * expr.grid_width
		frameNo = int(words[0])
		#print words, frameNo, frameData
		while len(frameData) < frameNo + 1:
			frameData.append([None] * (expr.grid_width * expr.grid_height))
		frameData[frameNo][cellNo] = [float(words[2]), -float(words[3])]
		# assert that the cells are in ascending order and starting from 0
		assert len(frameData[frameNo]) > cellNo
		if(expr.start <= frameNo < expr.end):
			if(frameNo != lastFrame):
				lastFrame = frameNo
				frameCount += 1
			if(conData[cellNo] == None):
				cellData = [float(words[2]),-float(words[3])]
				conData[cellNo] = cellData
			else:
				conData[cellNo][0] += float(words[2])
				conData[cellNo][1] += -float(words[3])
			#print 	words[2], "  ", words[3]

	for cell in range(len(conData)):
		conData[cell][0] /= frameCount
		conData[cell][1] /= frameCount
	return frameData,conData
	
def process(file="manual.xml.csv",gridw=4,gridh=3,width=320,height=240,scale=.3, start=0, end=1000000):
	conData = readData(file=file,start=start,end=end)
	#print conData
	mantrac.plotVectors(conData=conData,gridw=gridw,gridh=gridh,width=width,height=height,scale=scale)
	pylab.axis([0,width,0,height])
	pylab.show()
	
#process()


class ConsolidatedDataPlot:
	def __init__(self,master):
		self.frame = tk.Frame(master)
		self.frame.pack()
		
		self.fileNameL = tk.Label(self.frame,text='Input Datafile name')
		self.fileNameL.pack(side=tk.TOP)
		self.fileName = tk.StringVar(self.frame,"manual.xml.csv","fileName")
		self.fileNameE = tk.Entry(self.frame,textvariable=self.fileName,width=20)
		self.fileNameE.pack(side=tk.TOP)
		
#		self.outPutL = tk.Label(self.frame,text='Output Filename')
#		self.outPutL.pack(side=tk.TOP)
#		self.outPut = tk.StringVar(self.frame,"data.txt","outPut")
#		self.outPutE = tk.Entry(self.frame,textvariable=self.outPut,width=20)
#		self.outPutE.pack(side=tk.TOP)
		
		self.widthL = tk.Label(self.frame,text='self.frame width')
		self.widthL.pack(side=tk.TOP)
		self.width = tk.IntVar(self.frame,"320","width")
		self.widthE = tk.Entry(self.frame,textvariable=self.width)
		self.widthE.pack(side=tk.TOP)
		
		self.heightL = tk.Label(self.frame,text='self.frame height')
		self.heightL.pack(side=tk.TOP)
		self.height = tk.IntVar(self.frame,"240","height")
		self.heightE = tk.Entry(self.frame,textvariable=self.height)
		self.heightE.pack(side=tk.TOP)
		
		self.gridwL = tk.Label(self.frame,text='grid width')
		self.gridwL.pack(side=tk.TOP)
		self.gridw = tk.IntVar(self.frame,"8","gridw")
		self.gridwE = tk.Entry(self.frame,textvariable=self.gridw)
		self.gridwE.pack(side=tk.TOP)
		
		self.gridhL = tk.Label(self.frame,text='grid height')
		self.gridhL.pack(side=tk.TOP)
		self.gridh = tk.IntVar(self.frame,"6","gridh")
		self.gridhE = tk.Entry(self.frame,textvariable=self.gridh)
		self.gridhE.pack(side=tk.TOP)
		
		self.scaleL = tk.Label(self.frame,text='scale (only graphic)')
		self.scaleL.pack(side=tk.TOP)
		self.scale = tk.DoubleVar(self.frame,"40","scale")
		self.scaleE = tk.Entry(self.frame,textvariable=self.scale)
		self.scaleE.pack(side=tk.TOP)
		
		self.startL = tk.Label(self.frame,text='start self.frame')
		self.startL.pack(side=tk.TOP)
		self.start = tk.IntVar(self.frame,"0","start")
		self.startE = tk.Entry(self.frame,textvariable=self.start)
		self.startE.pack(side=tk.TOP)
		
		self.endL = tk.Label(self.frame,text='end self.frame')
		self.endL.pack(side=tk.TOP)
		self.end = tk.IntVar(self.frame,"1000000","end")
		self.endE = tk.Entry(self.frame,textvariable=self.end)
		self.endE.pack(side=tk.TOP)
				
		self.AcceptB = tk.Button(self.frame,text='Show')
		self.AcceptB.pack(side=tk.TOP)
		self.AcceptB.bind("<Button-1>",self.showPlot)
	
	def showPlot(self,event):
		#print "Got click"
		try:
			f = open(self.fileName.get(),'r')
			f.close()
		except IOError:
			tkMessageBox._show("File Error"\
			,"Unable to open "+os.path.abspath(self.fileName.get())\
			,icon=tkMessageBox.ERROR\
			,type=tkMessageBox.OK)
			return
			
		try:
			process(file=self.fileName.get()\
			,width=self.width.get()\
			,height=self.height.get()\
			,gridw=self.gridw.get()\
			,gridh=self.gridh.get()\
			,scale=self.scale.get()
			,start=self.start.get()\
			,end=self.end.get()\
			)
		except Exception, inst:
			#traceback.print_exc(file=sys.stdout)
			print str(inst)
			tkMessageBox._show("Some Error"\
			,"The processing of the XML file could not be completed\n"+str(inst)\
			,icon=tkMessageBox.ERROR\
			,type=tkMessageBox.OK\
			,parent=self.frame)
			return
		return

def showGui():
	
	root = tk.Tk()
	m = ConsolidatedDataPlot(root)
	root.mainloop()
		
if __name__ == '__main__':
	showGui()
