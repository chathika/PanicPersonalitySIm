#!/usr/bin/python 
# -*- coding: utf-8 -*-
import mantrac
import pylab
import re
#import Tkinter as tk
import plotdata
from copy import deepcopy
#import numpy
import sys
#import tkMessageBox
import stats
import os.path
import cfgparse
import subprocess
import platform
from shutil import copyfile
from math import sqrt
from mantrac import Point3D,Plane3D,Experiment
import Queue
#import socket
import threading
import  time

opts = None
mtof_output_columns = "#Name,  Video Path,  XML file Path, Aperture , Threshold, start, end, skip, gridw, gridh, width, height, flux_type, V Corr R, V Corr I, V Scale R, V Scale I, V Off R, V Off I, V B, V Theta, X r, X r2, X gradient, X p, X ESQ, Y r, Y r2, Y gradient, Y p, Y ESQ, Mag r, Mag r2, Mag gradient, Mag p, Mag ESQ, K AlphaC M, K AlphaC STDEV,K AlphaF M, K AlphaF STDEV, K AlphaV, K AlphaV Avg SSQ"
simmt_output_columns = "#Name,  Video Path,  XML file Path, Aperture , Threshold, start, end, skip, gridw, gridh, viewport_width,viewport_height,flux_type,V Corr R, V Corr I, V Scale R, V Scale I, V Off R, V Off I, V B, V Theta, X r, X r2, X gradient, X p, X ESQ, Y r, Y r2, Y gradient, Y p, Y ESQ, Mag r, Mag r2, Mag gradient, Mag p, Mag ESQ, K AlphaC M, K AlphaC STDEV,K AlphaF M, K AlphaF STDEV, K AlphaV, K AlphaV Avg SSQ, Density R"
mtof_input_columns = "#Name,  Video Path,  XML file Path, Aperture , Threshold, start, end, skip, gridw, gridh, width, height,fps,flux_type,flux_converter_name,cosine_bell_smoothing_pieces"

simof_input_columns = "#Name,  Video Path,  CSV file Path, Aperture , Threshold, start, end, skip, gridw, gridh, width, height(,a,b,c,w,z, po_x,po_y,p2_x,p2_y,wox,woy)"
simmt_input_columns = "#Name,  CSV file Path, XML File Path, pixels per meter, <spare field> , start, end, skip, gridw, gridh, video_width, video_height,sim_width,sim_height,vieport_width,vieport_height,video_fps,flux_type,flux_converter_name,cosine_bell_smoothing_pieces,a,b,c,w,z, po_x,po_y,p2_x,p2_y,wox,woy,gridwidth,gridheight"

# Documentation
"""

Arrangement of this file (In order).
* Optical flow functions - Functions that help with converting the video into statistics
* Configuration file processing - Processes the common configuration file
* Experiment file processing functions - Functions that help process the experiment file into data that can be easily accessed by the experiment functions.
* Comparison functions - functions that generates correlation data from processed data. E.g., Given manual tracking stats and optical flow stats, generates correlation statistics	
* Experiment functions - Functions which Generates the statistical data for each side of the experiment for each experiment. Includes function for processing all the experiments.
* Main.




"""

# -------------------------------------------------------------------------
# Optical Flow functions


def processVideo(expr):
	if re.match(r'^.*\.csv$',expr.videoStr):
		print "\tVideo file has csv extension. Assuming it is preprocessed video data."
		return video
	dirname = os.path.dirname(expr.videoStr)+'\\'
	filename = os.path.basename(expr.videoStr)
	
	if re.match(r'^.*\.avi$',filename) :
		filename = filename[0:-4]
	#print "TODO: code processVideo "
	
	opts = readFile()
	if expr.start - expr.step >= 1:
		expr.start -= expr.step
	#print "'"+opts.opticalflowexe+"'-'"+filename+"'"
	args = [opts.opticalflowexe,dirname,filename,"c:\\temp.csv","1",str(expr.aperture),str(expr.threshold),str(expr.start),str(expr.end),str(expr.step),str(expr.grid_width),str(expr.grid_height)]
        if platform.system() == 'Linux':
                args.insert(0,'wine')
	if False:
		for  arg in args:
			print arg,
		print
	returnval = subprocess.call(args)
	#print returnval
	if returnval != 0:
		import tkMessageBox
		tkMessageBox._show("Opticalflow Execution error"\
			,"The video file could not be processed. OpticalflowIST returned "+str(returnval)+""\
			,icon=tkMessageBox.ERROR\
			,type=tkMessageBox.OK)
		
	return "c:\\temp.csv"
	


#! -------------------------------------------------------------------------------
""" Error calculations (redundant) """
"""
def get_err(m1,m2):
	err = 0.
	for i in range(len(m1)):
		err += pow(m1[i] - m2[i],2)
	return err

def get_err_con(m1,m2,j):
	err = 0.
	for i in range(len(m1)):
			err += pow(m1[i][j] - m2[i][j],2)
			#print m1[i][j], m2[i][j]
	return err

def get_min_err(m1, m2):
	numerator = 0.
	denominator = 0.
	c = 0.
	for i in range(len(m1)):
		numerator += m1[i] * m2[i]
		denominator += m2[i] * m2[i]
	c = numerator/denominator
	m_scaled = [ c * v for v in m2 ]
	return get_err(m1,m_scaled),c

def get_min_err_con(manual_conData, optflow_conData,index):
	numerator = 0.
	denominator = 0.
	c = 0.
	for i in range(len(manual_conData)):
		for j in [0,1]:
			numerator += manual_conData[i][j] * optflow_conData[i][j]
			denominator += optflow_conData[i][j] * optflow_conData[i][j]
	c = numerator/denominatorn
	m_scaled = deepcopy(optflow_conData)
	for i in range(len(optflow_conData)):
		m_scaled[i][index] *= c
	
	
	return get_err_con(manual_conData,m_scaled,index),c

def test_error():
	if(get_err(a,a) != 0):
		print "sum sq error between the same matrices " + str(a)
	if get_err(a,d) == 0:
		print "no sum sq error between the different matrices " + str(a) + ", " + str(d)
	r1,r2 = get_min_err(a,b)
	print r1, r2
	if r1 != 0:
		print "scaling does not work " + str(a) + ", " + str(b)

def get_min_err_con_all(manual_conData, optflow_conData):
	xe,xc = get_min_err_con(manual_conData,optflow_conData,0)
	#print xe,xc
	ye,yc = get_min_err_con(manual_conData,optflow_conData,1)
	#print ye,yc
	return xe+ye

def test_error_con():
	if(get_err_con(manual_conData,manual_conData,0) != 0):
		print "sum sq error between the same data "
	if(get_err_con(manual_conData,manual_conData,1) != 0):
		print "sum sq error between the same data on x"
"""


	
	
	
	
#--------------------------------------------------------------
# Configuration file access functions

def readFile():
	c = cfgparse.ConfigParser()
	c.add_option('opticalflowexe', type='string')
	c.add_file('config.ini')
	return c.parse()


	
#---------------------------------------------------------
# Experiment file processing
	
def processLine_mtof(expr,words):
	i = 3
	expr.aperture = int(words[i])
	i += 1
	expr.threshold = int(words[i])
	i += 1
	expr.start = int(words[i])
	i += 1
	expr.end = int(words[i])
	i += 1
	expr.step = int(words[i])
	i += 1
	expr.grid_width = int(words[i])
	i += 1
	expr.grid_height = int(words[i])
	i += 1
	expr.video_width = int(words[i]) #10
	i += 1
	expr.video_height = int(words[i]) 
	i += 1
	expr.video_fps = float(words[i])
	i += 1
	expr.flux_type = words[i]
	i += 1
	expr.flux_converter_name = words[i]
	i += 1
	expr.cosine_bell_smoothing_pieces = float(words[i])
	
	expr.cell_width = expr.video_width / expr.grid_width
	expr.cell_height = expr.video_height / expr.grid_height
	expr.width = expr.video_width
	expr.height = expr.video_height
	return
	#return xl
	
def processLine_simof(expr,words):
	i = 3
	expr.aperture = int(words[i])
	i += 1
	expr.threshold = int(words[i])
	i += 1
	expr.start = int(words[i])
	i += 1
	expr.end = int(words[i])
	i += 1
	expr.step = int(words[i])
	i += 1
	expr.grid_width = int(words[i])
	i += 1
	expr.grid_height = int(words[i])
	i += 1
	expr.video_width = int(words[i]) #10
	i += 1
	expr.video_height = int(words[i]) 
	i += 1
	expr.simulation_width = float(words[i]) 
	i += 1
	expr.simulation_height = float(words[i]) 
	i += 1
	expr.video_fps = float(words[i])
	i += 1
	expr.flux_type = words[i]
	i += 1
	expr.flux_converter_name = words[i]
	i += 1
	if(len(words) > i):
		expr.have_plane = 1
		expr.have_sim_anchor = 1
		expr.a = float(words[i]) #17
		i += 1
		expr.b = float(words[i])
		i += 1
		expr.c = float(words[i])
		i += 1
		expr.w = float(words[i])
		i += 1
		expr.z = float(words[i]) 
		i += 1
		expr.po_x = float(words[i])
		i += 1
		expr.po_y = float(words[i]) 
		i += 1
		expr.p2_x = float(words[i])
		i += 1
		expr.p2_y = float(words[i])
		i += 1
		expr.wo_x = float(words[i])
		i += 1
		expr.wo_y = float(words[i])

	expr.cell_width = expr.video_width / expr.grid_width
	expr.cell_height = expr.video_height / expr.grid_height
	expr.width = expr.video_width
	expr.height = expr.video_height

	return None

def processLine_simmt(expr,words):
	i=3
	expr.pixels_per_meter = int(words[i])
	i += 1
	words[i] # spare field
	i += 1
	expr.start = int(words[i])
	i += 1
	expr.end = int(words[i])
	i += 1
	expr.step= int(words[i])
	i += 1
	expr.grid_width = int(words[i])
	i += 1
	expr.grid_height = int(words[i])
	i += 1
	expr.video_width = int(words[i]) #10
	i += 1
	expr.video_height = int(words[i]) 
	i += 1
	expr.simulation_width = float(words[i]) 
	i += 1
	expr.simulation_height = float(words[i]) 
	i += 1
	expr.viewport_width = int(words[i])
	i += 1
	expr.viewport_height = int(words[i]) 
	i += 1
	expr.video_fps = float(words[i]) 
	i += 1
	expr.flux_type = words[i]
	i += 1
	expr.flux_converter_name = words[i]
	i += 1
	expr.cosine_bell_smoothing_pieces = float(words[i])
	i += 1
	if(len(words) > i):
		expr.have_plane = 1
		expr.a = float(words[i])  #19
		i += 1
		expr.b = float(words[i])
		i += 1
		expr.c = float(words[i]) 
		i += 1
		expr.w = float(words[i])
		i += 1
		expr.z = float(words[i])
		i += 1
	if(len(words) > i):
		expr.have_sim_anchor = 1
		expr.po_x = float(words[i]) #24
		i += 1
		expr.po_y = float(words[i]) 
		i += 1
		expr.p2_x = float(words[i]) 
		i += 1
		expr.p2_y = float(words[i])
		i += 1
		expr.wo_x = float(words[i])
		i += 1
		expr.wo_y = float(words[i])
	expr.cell_width = expr.viewport_width / expr.grid_width
	expr.cell_height = expr.viewport_height / expr.grid_height
	expr.width = expr.viewport_width
	expr.height = expr.viewport_height
	return None
	
def readList(file):
	" Reads the configuration file and converts in to a list of list of parameters. The paramters are as follows.\nName, Video file, XML file, etc"
	exprs = []
	f = []
	try:
		f = open(file,'r')
	except IOError:
		import tkMessageBox
		tkMessageBox._show("Some Error"\
			,file+" could not be opened. Create a CSV file with these columns\n\n"+simof_input_columns \
			,icon=tkMessageBox.ERROR\
			,type=tkMessageBox.OK)
		return xl
		
	linec = 0
	for line in f:
		expr = Experiment()
		linec += 1
		words = re.split("\s*,\s*|^\s*",line[:-1])
		if words[0] == '':
			words = words[1:]
		if(len(words) >= 5 and (not re.match("^#", words[0]))):		
			exprs.append(expr)
			expr.name = words[0]
			haveXML = 0
			haveCSV = 0
			haveMovie = 0
			
			expr.source1str = words[1]
			expr.source2str = words[2]
			if re.match("^.*\.xml",words[1]):
				expr.source1type = "motiontrack"
				expr.motiontrackStr = expr.source1str
				haveXML = 1
			else:
				if re.match("^.*\.avi",words[1]):
					expr.source1type = "video"
					expr.videoStr = expr.source1str
					haveMovie = 1
				else:
					if re.match("^.*\.csv",words[1]):
						expr.source1type = "simlog"
						expr.simlogStr = expr.source1str
						haveCSV = 1
			if re.match("^.*\.xml",words[2]):
				expr.source2type = "motiontrack"
				expr.motiontrackStr = expr.source2str
				haveXML = 1
			else:
				if re.match("^.*\.avi",words[2]):
					expr.source2type = "video"
					expr.videoStr = expr.source2str
					haveMovie = 1
				else:
					if re.match("^.*\.csv",words[2]):
						expr.source2type = "simlog"
						expr.simlogStr = expr.source2str
						haveCSV = 1
			
			if haveXML + haveCSV + haveMovie != 2:
				print "Error in experiment file at line", linec
			"Movie, XML" 
			if haveXML and haveMovie:
				expr.comparison_type = "ofmt"
				if re.match("^.*\.xml",words[1]):
					temp = words[1]
					words[1] = words[2]
					words[2] = words[1]
				processLine_mtof(expr,words)
			"Movie, Sim"
			if haveCSV and haveMovie:
				expr.comparison_type = "ofsim"
				if re.match("^.*\.csv",words[1]):
					temp = words[1]
					words[1] = words[2]
					words[2] = words[1]
				processLine_simof(expr,words)
			"XML, Sim"
			if haveXML and haveCSV:
				expr.comparison_type = "simmt"
				if re.match("^.*\.xml",words[1]):
					temp = words[1]
					words[1] = words[2]
					words[2] = words[1]
				processLine_simmt(expr,words)
				
			# set default values here
			expr.people_height = 1.5
	f.close()
	return exprs
	

def extractValues(conData):
	return [[x[0],x[1]] for x in conData]	
	
def extractCount(conData):
	return [x[2] for x in conData]	
	

# ------------------------------------------------------------------------
# Data comparison functions

def compare(m1,o1,scale=1):
	m = deepcopy(m1)
	o = deepcopy(o1)
	
	for i in range(len(m)):
		m[i][0] *= scale
		m[i][1] *= scale
		o[i][0] *= scale
		o[i][1] *= scale
	
	mx = [v[0] for v in m]
	my = [v[1] for v in m]
	mm = mag(m)
	ox = [v[0] for v in o]
	oy = [v[1] for v in o]
	om = mag(o)
	#print m,m,o
	#print "Sum of squared error (pixels per second): " + str(get_min_err_con_all(m,o))
	#print "--------------"
	#print mx,ox,my,oy
	
	xgr,xi,xr,xp,xerr = stats.linregress(mx,ox)
	ygr,yi,yr,yp,yerr = stats.linregress(my,oy)
	maggr,magi,magr,magp,magerr = stats.linregress(mm,om)
	cn_corr,cn_scale,cn_offset = mantrac.cn_corr(o,m)
	
	return str(cn_corr[0]) + ", " + str(cn_corr[1]) + ", " + str(cn_scale[0]) + "," + str(cn_scale[1]) + \
	", " + str(cn_offset[0]) + ", " + str(cn_offset[1]) + ", " + "TODO" + ", " + "TODO" + ", " + str(xr) + \
	", " + str(xr**2) + ", " + str(xgr) + ", " + str(xp) + " , " + str(xerr) + \
	", " + str(yr) +\
	", " + str(yr**2) + ", " + str(ygr) + ", " + str(yp) + "," + str(yerr) + \
	", " + str(magr) + \
	", " + str(magr**2) + ", " + str(maggr) + ", " + str(magp) + "," + str(magerr)

def compare_kaup(u,v,fstart,fend,fstep):
 	#print len(u),len(v)
	#for i in range(0,48):
	#	print v[96][i],
	#print
	frameuv = [0] * (fend+1)
	frameusq = [0] * (fend+1)
	
	fmax = len(u)
	cmax = len(u[fstart])
	
	for f in range(fstart,fend,fstep):
		#print f,
		frameuv[f] = []
		frameusq[f] = []
		#frameerr[f] = []
		for c in range(cmax):
			temp = frameuv[f]
			#print f
			#print c
			#print v[0],v[1],v[2],v[3],v[4]
			#print v
			temp = u[f][c][0]
			temp = v[f][c][0]
			uv = u[f][c][0] * v[f][c][0] + u[f][c][1] * v[f][c][1]
			usq = v[f][c][0] ** 2 + v[f][c][1] ** 2
			frameuv[f].append(uv)
			frameusq[f].append(usq)
			#alpha = uv * 1./usq
			#error = 
			#frameerr[f].append()
			#if f == 101
	#print frameuv[fstart]
	#print frameusq[fstart]
	fc = (fend - fstart)/fstep + 1
	#print "fc =",fc
	sumc_uv = [0] * cmax
	sumc_usq = [0] * cmax
	sumf_uv = [0] * fc
	sumf_usq = [0] * fc
	sumv_uv = 0
	sumv_usq = 0
	
	for f in range(fstart,fend,fstep):
		for c in range(cmax):
			sumc_uv[c] += frameuv[f][c]
			sumc_usq[c] += frameusq[f][c]
			sumf_uv[(f-fstart)/fstep] += frameuv[f][c]
			sumf_usq[(f-fstart)/fstep] += frameusq[f][c]
			sumv_uv += frameuv[f][c]
			sumv_usq += frameusq[f][c]
			
	alphac = [None] * cmax
	alphaf = [None] * fc
	alphav = 0
	
	for c in range(cmax):
		if sumc_usq[c] == 0:
			alphac[c] = None
		else:
			alphac[c] = sumc_uv[c] * 1.0 / sumc_usq[c]
		
	fi = -1
	try:
		for fi in range(len(alphaf)):
			alphaf[fi] = sumf_uv[fi] *1.0 / sumf_usq[fi]
	except ZeroDivisionError:
		print "ZeroDivisionError: fi =",fi
		

	alphav = sumv_uv *1.0 / sumv_usq
	alphav_error = 0
	alphav_count = 0
	
	for f in range(fstart,fend,fstep):
		for c in range(cmax):
			error = ((u[f][c][0] - v[f][c][0] * alphav) ** 2 + (u[f][c][1] - v[f][c][1] * alphav) ** 2)
			alphav_error += error
			alphav_count += 1
	
	#print "ALPHAF ",alphaf
	n,meanc,devc = mantrac.stats_describe(alphac)
	n,meanf,devf = mantrac.stats_describe(alphaf)
	return ","+str(meanc) + ","+str(devc) + ","+str(meanf) + ","+str(devf) + ","+str(alphav)+","+str(sqrt(alphav_error/alphav_count))
	
def compare_density(densities1,densities2):
	dgr,di,dr,dp,derr = stats.linregress(densities1,densities2)
	return ","+str(dr)
	
def save_density_comparison(f,densities1,densities2,gridw,gridh):
	for y in range(0,gridh):
		for x in range(0,gridw):
			s = str(densities1[y*gridw+x])+","+str(densities2[y*gridw+x])
			f.write(s)
			for i in range(len(s),15):
				f.write(" ")
		f.write("\n")
	
	
def mag(cart):
	"Magnitude of a cartesian value."
	result = [0] * len(cart)
	for i in range(len(cart)):
		result[i] = pow(pow(cart[i][0],2) + pow(cart[i][1],2),.5)
	return result
	
#----------------------------------------
# The core functions that process different types of experiments.
def process_simmt(expr,f):
	w_plane = None
	if expr.have_plane:
		print "Found the plane. Will use it for reverse projection of tracking data."
		g = mantrac.Plane3D(expr.a,expr.b,expr.c,expr.w)
		w_head_plane = g.move_plane(-(expr.people_height))
		
	if expr.have_sim_anchor:
		print "Found anchor points b/w video and simulation. Will use it for spatial comparison."
	else :
		po_x = None
		
	#DEBUG
	tempx,tempy =  mantrac.Plane3D.get_2d_coordinates_on_w(g,expr.z,mantrac.Point3D(0 - expr.video_width/2,expr.video_height/2 - 0,0),mantrac.Point3D(expr.po_x,expr.po_y,0),mantrac.Point3D(expr.p2_x,expr.p2_y,0),1)
	print tempx + 5.9,tempy + 7.9
	tempx,tempy =  mantrac.Plane3D.get_2d_coordinates_on_w(g,expr.z,mantrac.Point3D(320 - expr.video_width/2,expr.video_height/2 - 0,0),mantrac.Point3D(expr.po_x,expr.po_y,0),mantrac.Point3D(expr.p2_x,expr.p2_y,0),1)
	print tempx + 5.9,tempy + 7.9
	tempx,tempy =  mantrac.Plane3D.get_2d_coordinates_on_w(g,expr.z,mantrac.Point3D(0 - expr.video_width/2,expr.video_height/2 - 240,0),mantrac.Point3D(expr.po_x,expr.po_y,0),mantrac.Point3D(expr.p2_x,expr.p2_y,0),1)
	print tempx + 5.9,tempy + 7.9
	tempx,tempy =  mantrac.Plane3D.get_2d_coordinates_on_w(g,expr.z,mantrac.Point3D(320 - expr.video_width/2,expr.video_height/2 - 240,0),mantrac.Point3D(expr.po_x,expr.po_y,0),mantrac.Point3D(expr.p2_x,expr.p2_y,0),1)
	print tempx + 5.9,tempy + 7.9

	print "Processing the Simulation Data\n\tLoading data"
	sim_paths = mantrac.getPathsInLogFile(expr.simlogStr,expr,fps=expr.video_fps)
	print "\tCropping and scaling"
	#print paths2[50]
	if expr.have_sim_anchor:
		for i in range(0,len(sim_paths)):
			sim_paths[i] = sim_paths[i].cropAndScale(expr,expr.wo_x,expr.wo_y,1000,invertyheight=expr.simulation_height,invertyheight2=expr.viewport_height)
	#print paths2
	#print paths2[60]
	print "\tGenerating points"
	for path in sim_paths:	
		path.generatePoints(expr.start,expr.end,expr.step)
	
	"""# create and save projected simulation paths
	altEnd = endFrame
	sim_paths = mantrac.getPathsInLogFile(comparison[1],fps=framesPerSecond)
	for path in sim_paths:	
		path.generatePoints(startFrame,altEnd,step)
	sim_projected_paths = mantrac.project_to_screen(sim_paths,g,z,Point3D(po_x,po_y,0),Point3D(p2_x,p2_y,0),Point3D(wox,woy,0),width,height)
	for path in sim_projected_paths:	
		path.generatePoints(startFrame,altEnd,step)
	mantrac.savePathsCVS(sim_projected_paths,startFrame,altEnd,step,"sim_projected_paths.cvs")
	"""
	
	#DEBUG
	"""if 0:
		mantrac.plotPaths(sim_projected_paths,start=startFrame,end=altEnd,step=step,width=width,height=height)
		pylab.axis([0,width,0,height])
		pylab.show()
	frame = Plane3D.from_normal(Point3D(0,0,z),Point3D(0,0,1))
	origin_2d = Point3D(po_x,po_y,z)
	x_axis_2d = Point3D(p2_x,p2_y,z)
	g_point,g_normal = g.get_point_normal()
	origin = g.vector_intersection(origin_2d)
	x_axis = g.vector_intersection(x_axis_2d).subtract(origin).normalize()
	y_axis = g_normal.cross_prod(x_axis).normalize()
	for dist in range(0,10):
		f3 = frame.vector_intersection(origin.add(x_axis.scalar_prod(dist)))
		print dist,"meters along x-axis",f3.x + width/2,height/2 - f3.y
	for dist in range(0,10):
		f4 = frame.vector_intersection(origin.add(y_axis.scalar_prod(dist)))
		print dist,"meters along y-axis",f4.x + width/2,height/2 - f4.y
	hp = Plane3D.from_normal(g_point.add(g_normal.scalar_prod(-1.7)),g_normal)
	head1_2d = Point3D(114 - width/2,height/2 - 164,z)
	head1 = hp.vector_intersection(head1_2d)
	ground1 = head1.add(g_normal.scalar_prod(1.7))
	feet1_2d = frame.vector_intersection(ground1)
	print "feet1_2d",feet1_2d.x + width/2, height/2 - feet1_2d.y
	ground1_relative = ground1.subtract(origin)
	print ground1_relative
	print "point1 components",ground1_relative.dot_prod(x_axis),ground1_relative.dot_prod(y_axis)"""

	print "\tExtracting Data",
	vels2 = mantrac.get_all_velocity(sim_paths,expr.start,expr.end,expr.step)
	print ".",
	velspp2 = mantrac.get_velocity_per_person(sim_paths,expr.start,expr.end,expr.step)
	print ".",
	distpp2 = mantrac.get_distance_per_person(sim_paths,expr.start,expr.end,expr.step)
	print ".",
	dist2 = []
	for distp2 in distpp2:
		dist2.extend(distp2)
	print "."
	
	print "Processing the tracking Data:Loading"
	xmlData = mantrac.get_a_document(expr.motiontrackStr)
	mantrac.delete_interpolation_nodes(xmlData)
	if expr.have_plane:
		if expr.have_sim_anchor:
			print "\tCreating paths w.r.t anchors"
			paths = mantrac.xmlToPaths(xmlData,expr,1000)#w_head_plane,expr.z,width,height,mantrac.Point3D(po_x,po_y,0),mantrac.Point3D(p2_x,p2_y,0),1000,square_width,square_height)
		else:
			print "\tCreating paths w.r.t default origin"
			paths = mantrac.xmlToPaths(xmlData,expr)
	else:
		paths = mantrac.xmlToPaths(xmlData,expr)
	#paths = [paths[2]]
	#print paths[0]
	#print mantrac.xmlToPaths(xmlData)[2]
	#print paths[2]
	print "\tCreating points"
	for path in paths:
		path.generatePoints(expr.start,expr.end,expr.step)
	

	#mantrac.savePaths(paths,startFrame,endFrame,step)
	print "\tExtracting Data",
	vels = mantrac.get_all_velocity(paths,expr.start,expr.end,expr.step)
	print ".",
	velspp = mantrac.get_velocity_per_person(paths,expr.start,expr.end,expr.step)
	print ".",
	distpp = mantrac.get_distance_per_person(paths,expr.start,expr.end,expr.step)
	print ".",
	#dist = mantrac.get_all_distance(paths,expr.start,expr.end,expr.step)
	dist = []
	for distp in distpp:
		dist.extend(distp)
	print "."
	
	print "Saving the simulation and tracking data as graphs"
	if expr.have_plane:
		expr.pixels_per_meter = 1
	mantrac.save_velocity_histogram(vels,vels2,expr.name,expr.viewport_width/1000,pixelsperm=expr.pixels_per_meter)
	mantrac.save_velocity_graph_pp(velspp,velspp2,expr.name,pixelsperm=expr.pixels_per_meter)
	mantrac.save_distance_histogram(dist,dist2,expr.name,expr.viewport_width/100,pixelsperm=expr.pixels_per_meter)
	mantrac.save_distance_graph_pp(distpp,distpp2,expr.name,pixelsperm=expr.pixels_per_meter)
	
	print "Comparing simulation and tracking data using various metrics."
	f.write(expr.source1str + ", " + \
		expr.source2str + ", " + \
		str(expr.pixels_per_meter) + ", " + \
		str(expr.start) + ", " + \
		str(expr.end) + ", " + \
		str(expr.step) + ", " + \
		str(expr.grid_width) + ", " + \
		str(expr.grid_height) + ", " + \
		str(expr.video_width) + ", " + \
		str(expr.video_height) + ", " + \
		str(expr.viewport_width) + ", " + \
		str(expr.viewport_height) + ", " + \
		str(expr.flux_type) + ", ")
		
	#trackingData
	frameData,conData = mantrac.process(paths,expr,scale = 10,showPath = True,showGraph = False,sourceType='paths',outPut = None)

	#simulationData
	frameData2,conData2 = mantrac.process(sim_paths,expr,scale = 10,showPath = True,showGraph = 'sim',sourceType='paths',outPut = 'sim')
	
	f.write(compare(extractValues(conData),extractValues(conData2)))
	f.write(compare_kaup(frameData,frameData2,expr.start,expr.end,expr.step))
	count1 = extractCount(conData)
	count2 = extractCount(conData2)
	f.write(compare_density(count1,count2))
	f.write("\n")
	df = open(expr.name+"_density.txt","w")
	save_density_comparison(df,count1,count2,expr.grid_width,expr.grid_height)
	df.close()
	print "Done saving the data."
	#f.close()
	
	
def process_ofsim(expr,f):
	if expr.have_plane and expr.have_sim_anchor:
		g = mantrac.Plane3D(expr.a,expr.b,expr.c,expr.w)
		w_head_plane = g.move_plane(-(expr.people_height))
		
		altEnd = expr.end
		sim_paths = mantrac.getPathsInLogFile(expr.simlogStr,fps=expr.video_fps)
		for path in sim_paths:	
			path.generatePoints(expr.start,altEnd,expr.step)
		sim_projected_paths = mantrac.project_to_screen(sim_paths,expr,g)
		for path in sim_projected_paths:	
			path.generatePoints(expr.start,altEnd,expr.step)
		paths = sim_projected_paths
	else:
		paths = mantrac.getPathsInLogFile(expr.simlogStr,fps=expr.video_fps)

	#frameData,conData = mantrac.process(paths,expr,scale = 1,showPath = False,showGraph = False,sourceType='paths')
	frameData,conData = mantrac.process(paths,expr,scale = 10,showPath = True,showGraph = True,sourceType='paths')
	
	videoFile = processVideo(expr)
	
	f.write(expr.name+", ")
	if(videoFile == None):
		f.write("\n")
	else:
		frameData2, conData2 = plotdata.readData(videoFile,expr)
		f.write(expr.source1str + ", " + \
		expr.source2str + ", " + \
		str(expr.aperture) + ", " + \
		str(expr.threshold) + ", " + \
		str(expr.start) + ", " + \
		str(expr.end) + ", " + \
		str(expr.step) + ", " + \
		str(expr.grid_width) + ", " + \
		str(expr.grid_height) + ", " + \
		str(expr.video_width) + ", " + \
		str(expr.video_height) + ", " + \
		str(expr.flux_type) + ", ")
		f.write(compare(extractValues(conData),conData2))
		f.write(compare_kaup(frameData,frameData2,expr.start,expr.end,expr.step))
		f.write("\n")
	
def process_ofmt(expr,f):

	print "Processing manual tracking data"
	frameData,conData = mantrac.process(expr.motiontrackStr,expr,scale = 1,showPath = False,showGraph = False)
	#frameData,conData = mantrac.process(expr.motiontrackStr,expr,scale = 10,showPath = True,showGraph = True)
	
	print "Processing video"
	videoFile = processVideo(expr)
	
	f.write(expr.name + ", ")
	if(videoFile == None):
		f.write("\n")
	else:
		print "Loading video data"
		frameData2, conData2 = plotdata.readData(videoFile,expr)
		f.write(expr.source1str + ", " + \
			expr.source2str + ", " + \
			str(expr.aperture) + ", " + \
			str(expr.threshold) + ", " + \
			str(expr.start) + ", " + \
			str(expr.end) + ", " + \
			str(expr.step) + ", " + \
			str(expr.grid_width) + ", " + \
			str(expr.grid_height) + ", " + \
			str(expr.video_width) + ", " + \
			str(expr.video_height) + ", " + \
			str(expr.flux_type) + ", ")
		#print extractValues(conData)
		print "Creating first comparison results"
		f.write(compare(extractValues(conData),conData2))
		print "Creating second comparison results"
		f.write(compare_kaup(frameData,frameData2,expr.start,expr.end,expr.step))
		f.write("\n")
		
		if None:
			#print conData
			#print conData2
			mantrac.plotVectors(conData2,expr,scale=.2)
			mantrac.plotVectors(conData,expr,scale=10,color='green')
			pylab.axis([0,expr.width,0,expr.height])
			pylab.show()

		
def process(file="list.txt"):
	exprs = readList(file)
	
	f = open("comparison_result.csv",'w')
	
	previous_type = ""
	for expr in exprs:
		print "Processing... "
		print expr
		if expr.comparison_type == "simmt":
			if previous_type  != expr.comparison_type:
				
				f.write(simmt_output_columns+"\n")
				previous_type = expr.comparison_type
			f.write(expr.name+",")
			process_simmt(expr,f)
		else:
			if expr.comparison_type == "ofsim":
				if previous_type  != expr.comparison_type:
					f.write(mtof_output_columns+"\n")
					previous_type = expr.comparison_type
				f.write(expr.name+",")
				process_ofsim(expr,f)
			else:
				if previous_type  != expr.comparison_type:
					f.write(mtof_output_columns+"\n")
					previous_type = expr.comparison_type
				f.write(expr.name+",")
				process_ofmt(expr,f)
				
			
	f.close();
	copyfile("comparison_result.csv",exprs[0].name+"_cmp.csv")
	

#----------------------------------------------------
# Threading

class ExperimentThread( threading.Thread ) :
	
	
	def __init__ ( self, q,ids) :
		self.exprPool = q
		self.ids = ids
		threading.Thread.__init__(self)
	
	
	def run( self ):
		
		done = False
		while not done:
			expr = self.exprPool.get()
			if expr != None and expr != []:
				"""if expr.comparison_type == "simmt":
				f.write(simmt_output_columns+"\n")
				process_simmt(expr,f)
				else:
					if expr.comparison_type == "ofsim":
						f.write(mtof_output_columns+"\n")
						process_ofsim(expr,f)
					else:
						f.write(mtof_output_columns+"\n")
						process_ofmt(expr,f)"""
			else:
				done = True
				
	
			
			

def process_exprs_threaded(exprs,threads = 8):
	
	exprPool = Queue.Queue(0)
	threadPool = []
	for expr in exprs:
		exprPool.put(expr)
		
	for i in range(threads):
		thread = ExperimentThread(exprPool,i)
		threadPool.append(thread)
		thread.start()
		exprPool.put([])
	
	"""done = False
	while not done:
		done = True
		time.sleep(5)
		for thread in threadPool:
			if thread.isAlive():
				print "A thread is alive"
				done = False"""
	for thread in threadPool:
		thread.join()
	
	
	
	
	
	
	
#---------------------------------------------------------------
# Main	
#paths = None
	
if __name__ == '__main__':
	print ""
	opts = readFile()
#	try:
	if False:
		paths2 = mantrac.getPathsInLogFile("dataLog.csv",fps=30)
		for path in paths2:
			path.generatePoints(0,100,5)
		mantrac.savePaths(paths2,0,100,5,"paths.txt")
		vels2 = mantrac.get_all_velocity(paths2,0,100,5)
		velspp2 = mantrac.get_velocity_per_person(paths2,0,100,5)
		xmlData = mantrac.get_a_document("manual.xml")
		mantrac.delete_interpolation_nodes(xmlData)
		paths = mantrac.xmlToPaths(xmlData)
		for path in paths:
			path.generatePoints(0,100,5)
		vels = mantrac.get_all_velocity(paths,0,100,5)
		velspp = mantrac.get_velocity_per_person(paths,0,100,5)
		mantrac.save_velocity_histogram(vels,vels2,"experiment",.2,pixelsperm=10)
		mantrac.save_velocity_graph_pp(velspp,velspp2,"experiment",pixelsperm=10)
	else:
		process("exps.csv")
#	except Exception, inst:
#		print str(inst)
#		import tkMessageBox
#		tkMessageBox._show("Some Error"\
#			,"The processing could not be completed\n"+str(inst)\
#			,icon=tkMessageBox.ERROR\
#			,type=tkMessageBox.OK)
