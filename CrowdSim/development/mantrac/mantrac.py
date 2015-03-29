#!/usr/bin/python
# -*- coding: utf-8 -*-
import xml.dom.minidom
import sys,os
import pylab
import Tkinter as tk
import tkMessageBox
import traceback
import re
import stats
import math
from math import sqrt,pow
from random import uniform,normalvariate
#------------------------------------------------------
# Index of functions
"""
1. 3D classes and functions
	These functions and classes are used for projection of the manual tracking and optical flow data from the video into the real world.
	the function project_xml_to_xml can be use to convert data from one xml file to another.
2. 




"""

#--------------------------------------------------------
# Experiment data object
class Experiment:
	""" --- Recommended fields ---
		name
		source1str
		source2str
		source1
		source2
		source1type = {"motiontrack","video","simlog"}
		source2type = {"motiontrack","video","simlog"}
		videoStr
		video_frameData
		video_conData
		motiontrackStr
		motiontrack_paths
		simlogStr
		simlog_paths
		comparison_type = {"mtof","simof","simmt"}
		flux_type = {"average","total"}
		flux_converter_name
		flux_model
		start
		end
		step
		video_width
		video_height
		width
		height
		pixels_per_meter
		video_fps
		viewport_width
		viewport_height
		simulation_width
		simulation_height
		grid_width
		grid_height
		cell_width
		cell_height
		aperture
		threshold
		have_plane
		a
		b
		c
		w
		z
		people_height
		have_sim_anchor
		po_x
		po_y
		p2_x
		p2_y
		wo_x
		wo_y
		cosine_bell_smoothing_pieces
		
	"""
	def __init__(self):
		self.name = None
		self.source1str = None
		self.source2str = None
		self.source1 = None
		self.source2 = None
		self.source1type =  None
		self.source2type  = None
		self.videoStr = None
		self.video_frameData = None
		self.video_conData = None
		self.motiontrackStr = None
		self.motiontrack_paths = None
		self.simlogStr = None
		self.simlog_paths = None
		self.comparison_type  = None
		self.flux_type  = None
		self.flux_converter_name = None
		self.flux_model = None
		self.start = None
		self.end = None
		self.step = None
		self.video_width = None
		self.video_height = None
		self.video_fps = None
		self.viewport_width = None
		self.viewport_height = None
		self.simulation_width = None
		self.simulation_height = None
		self.grid_width = None
		self.grid_height = None
		self.cell_width = None
		self.cell_height = None
		self.aperture = None
		self.threshold = None
		self.have_plane = None
		self.a = None
		self.b = None
		self.c = None
		self.w = None
		self.z = None
		self.have_sim_anchor = None
		self.po_x = None
		self.po_y = None
		self.p2_x = None
		self.p2_y = None
		self.wo_x = None
		self.wo_y = None
		self.cosine_bell_smoothing_pieces = 0
		return
		
	def clean(self):
		self.flux_model = None
		self.source1 = None
		self.source2 = None
		self.video_frameData = None
		self.video_conData = None
		self.motiontrack_paths = None
		self.simlog_paths = None

	def __str__(self):
		return "Name\t\t     " + str(self.name) + \
		"\nS1 " + str(self.source1type) + "\t     " + str(self.source1str) + \
		"\nS2 " + str(self.source2type) + "\t     " + str(self.source2str) + \
		"\nVideoStr\t     " + str(self.videoStr) + \
		"\nSimlogStr\t     " + str(self.simlogStr) + \
		"\nMotiontrackStr\t     " + str(self.motiontrackStr) + \
		"\nComparison_type\t     " + str(self.comparison_type) + \
		"\nFlux_type\t     " + str(self.flux_type) + \
		"\nStart\t\t     " + str(self.start) + \
		"\nEnd\t\t     " + str(self.end) + \
		"\nStep\t\t     " + str(self.step) + \
		"\nVideo_width\t     " + str(self.video_width) + \
		"\nVideo_height\t     " + str(self.video_height) + \
		"\nVideo_fps\t     " + str(self.video_fps) + \
		"\nViewport_width\t     " + str(self.viewport_width) + \
		"\nViewport_height\t     " + str(self.viewport_height) + \
		"\nSimulation_width     " + str(self.simulation_width) + \
		"\nSimulation_height    " + str(self.simulation_height) + \
		"\nGrid_width\t     " + str(self.grid_width) + \
		"\nGrid_height\t     " + str(self.grid_height) + \
		"\nCell_width\t     " + str(self.cell_width) + \
		"\nCell_height\t     " + str(self.cell_height) + \
		"\nAperture\t     " + str(self.aperture) + \
		"\nThreshold\t     " + str(self.threshold) + \
		"\nHave_plane\t     " + str(self.have_plane) + \
		"\nGround\t\t     (" + str(self.a) + ", " + str(self.b) + ", " + str(self.c) + ", " + str(self.w) + "), z" + ": " + str(self.z) + \
		"\nHave_sim_anchor\t     " + str(self.have_sim_anchor) + \
		"\nPO_x\t\t     " + str(self.po_x) + \
		"\nPO_y\t\t     " + str(self.po_y) + \
		"\nP2_x\t\t     " + str(self.p2_x) + \
		"\nP2_y\t\t     " + str(self.p2_y) + \
		"\nWOx\t\t     " + str(self.wo_x) + \
		"\nWOy\t\t     " + str(self.wo_y) + \
		"\nFlux_converter_name  " + str(self.flux_converter_name)
		
#---------------------------------------------------------
# Point to flux conversion classes
class PositionToFluxConverter:
	def __init__(self):
		return self
	def setGridSize(self,width,height):
		return self
	def getCoefficient(self,pointx, pointy,gridx,gridy):
		return None
		
class DefaultPositionToFluxConverter(PositionToFluxConverter):
	width = None
	height = None
	
	def __init__(self):
		return self
	def setGridSize(self,width,height):
		self.width = width
		self.height = height
		return
	
	def getCoefficient(self,pointx,pointy,gridx,gridy):
		deltax = (pointx - gridx * width)
		deltay = (pointy - gridy * height)
		if deltax > 0 and deltay > 0 and deltax < width and deltay < height:
			return 1
		else:
			return 0
			
			
class HumanPositionToFluxConverter(PositionToFluxConverter):
	def __init__(self,matrix,offsetx,offsety):
		"Makes a flux converter represented by the rasterized shape stored in matrix. The shape is placed such that the pixel in the shape denoted by offsetx,offsety is in the specified position. offsetx and offsety are indexed from 0."
		self.model = matrix
		self.model_height = len(matrix)
		if(self.model_height == 0):
			self.model_width = 0
		else:
			self.model_width = len(matrix[0])
		self.max_effect = 0
		for row in matrix:
			for value in row:
				self.max_effect += value
		self.anchor_offset_x = offsetx
		self.anchor_offset_y = offsety
		self.grid_width = None
		self.grid_height = None
		self.effect = None
		self.effect_length = None

	def setGridSize(self,width,height):
		print "Creating flux conversion matrix",
		self.grid_width = width
		self.grid_height = height
		self.effect_length = (max(int(width/self.model_unit),int(height/self.model_unit),self.anchor_offset_x,self.anchor_offset_y))
		self.effect = []
		for y in range(0,2*self.effect_length):
			self.effect.append([0] * (2 * self.effect_length))
		for y in range(0,2*self.effect_length):
			anchor_y = y - self.effect_length
			print ".",
			for x in range(0,2*self.effect_length):
				anchor_x = x - self.effect_length
				model_origin_x_wrt_grid_origin_x = anchor_x# - self.anchor_offset_x
				model_origin_y_wrt_grid_origin_y = anchor_y# - self.anchor_offset_y
				pixel_effect = 0
				for grid_pixel_x in range(0,int(width/self.model_unit)):
					for grid_pixel_y in range(0,int(height/self.model_unit)):
						pixel_x_wrt_model_origin_x = grid_pixel_x - model_origin_x_wrt_grid_origin_x
						pixel_y_wrt_model_origin_y = grid_pixel_y - model_origin_y_wrt_grid_origin_y
						if 0 <= pixel_x_wrt_model_origin_x <self.model_width and 0 <= pixel_y_wrt_model_origin_y < self.model_height:
							#print pixel_y_wrt_model_origin_y,pixel_x_wrt_model_origin_x
							pixel_effect += self.model[pixel_y_wrt_model_origin_y][pixel_x_wrt_model_origin_x]
				self.effect[y][x] = pixel_effect * 1.0 / self.max_effect
			#print self.effect[y]
		print "Done."
		
	def getCoefficient(self,anchor_x,anchor_y,gridx,gridy):
		x = (int(anchor_x/self.model_unit) - self.anchor_offset_x - int(gridx/self.model_unit)) + self.effect_length
		y = (int(anchor_y/self.model_unit) - self.anchor_offset_x - int(gridy/self.model_unit)) + self.effect_length
		if 0 <= y < len(self.effect) and 0 <= x < len(self.effect[0]):
			return self.effect[y][x]
		else: return 0
	
	@classmethod
	def make(cls,name):
		if name == "B1_3_camera":
			return HumanPositionToFluxConverter.make_B1_3_camera()
		else:
			if name == "B1_3_default":
				return HumanPositionToFluxConverter.make_B1_3_default()
			else:
				if name == "single_pixel":
					return HumanPositionToFluxConverter.make_single_pixel()

				else:
					if name == "iman":
						return HumanPositionToFluxConverter.make_B1_3_iman()
					
		print "ERROR: Requested flux converter could not be instantiated."
	
	@classmethod
	def make_B1_3_camera(cls):
		fluxor = HumanPositionToFluxConverter([[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1]],5,5)
		fluxor.model_unit = 1 # because the grid units for top view grid is in pixels.
		return fluxor
		
	@classmethod
	def make_single_pixel(cls):
		fluxor = HumanPositionToFluxConverter([[1]],0,0)
		fluxor.model_unit = 1 # because the grid units for top view grid is in pixels.
		return fluxor
		
	@classmethod
	def make_B1_3_default(cls):
		fluxor = HumanPositionToFluxConverter([[0,1,1,1,0],[1,1,1,1,1],[1,1,1,1,1],[1,1,1,1,1],[0,1,1,1,0]],2,2)
		fluxor.model_unit = 100 # because the grid usnits for top view grid is in millimeters.This makes the model a half meter square.
		return fluxor

	@classmethod
	def make_B1_3_iman(cls):
		fluxor = HumanPositionToFluxConverter([[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1,1,1]],5,2)
		fluxor.model_unit = 100 # because the grid usnits for top view grid is in millimeters.This makes the model a half meter square.
		return fluxor

	@classmethod
	def make_debug_default(cls):
		return HumanPositionToFluxConverter([[1,1,1],[1,1,1],[1,1,1]],1,1)
		
	
	#def setGridSize(self,width,height):
		
		

#---------------------------------------------------
# 3D classes and functions
class Point3D:
	
	def __init__(self,x1,y1,z1):
		self.x = float(x1)
		self.y = float(y1)
		self.z = float(z1)

	def distance(self,p2):
		return sqrt((self.x - p2.x)**2 + (self.y - p2.y)**2 + (self.z - p2.z)**2)
		
	def subtract(self,p2):
		return Point3D(self.x - p2.x,self.y-p2.y,self.z-p2.z)

	def add(self,p2):
		return Point3D(self.x + p2.x,self.y+p2.y,self.z+p2.z)
		
	def scalar_prod(self,a):
		return Point3D(self.x * a,self.y * a,self.z * a)
	
	def cross_prod(self,p):
		return Point3D(self.y * p.z - self.z * p.y, self.z * p.x - self.x * p.z, self.x * p.y - self.y * p.x)
		
	def dot_prod(self,p):
		return self.x * p.x + self.y * p.y + self.z * p.z
		
	def normalize(self):
		denom = 1/sqrt(self.x ** 2 + self.y **2 + self.z ** 2)
		return self.scalar_prod(denom)
		
	def __str__(self):
		return "Point("+str(self.x)+","+str(self.y)+","+str(self.z)+")"

	def get_2d_components(self, v1,v2):
		return 0,0
		
		

class Plane3D:
	def __init__(self, a, b, c, w):
		"Constructs a plane for the form ax + by + cz = w given a,b,c,w"
		self.a = float(a)
		self.b = float(b)
		self.c = float(c)
		self.w = float(w)
		
	@classmethod
	def from_normal(cls, p, n):
		"Constructs a plane from a point in the plane and a normal to the plane"
		return cls(n.x, n.y, n.z, p.x * n.x + p.y * n.y + p.z * n.z)
		
	@classmethod
	def from_points(cls,p1,p2,p3):
		"Constructs a plane from 3 non linear points in the plane"
		return cls.from_normal(p2, p2.subtract(p1).cross_prod(p2.subtract(p3)).normalize())
	
	
	@classmethod
	#dimension specific data
	def get_2d_coordinates_on_w(cls, w, z, f_point,origin_2d=None,x_axis_2d=None,scale=1):
		if not origin_2d:
			origin_2d = Point3D(-160,-120,z)
			w_ground = None
		else:
			origin_2d.z = z
			w_ground = w.move_plane(1.7)
			
		if not x_axis_2d:
			x_axis_2d = Point3D(-159,-120,z)
		else:
			x_axis_2d.z = z
			
		w_point,w_normal = w.get_point_normal()
		
		if w_ground :
			origin = w_ground.vector_intersection(origin_2d).add(w_normal.scalar_prod(-1.7))
			x_axis = w_ground.vector_intersection(x_axis_2d).add(w_normal.scalar_prod(-1.7)).subtract(origin).normalize()
		else:
			origin = w.vector_intersection(origin_2d)
			x_axis = w.vector_intersection(x_axis_2d).subtract(origin).normalize()
			
		y_axis = w_normal.cross_prod(x_axis).normalize()
		
		
		real_point = w.vector_intersection(Point3D(f_point.x,f_point.y,z))
		relative_point = real_point.subtract(origin)
		
		#DEBUG
		#print y_axis
		#print "f_point",Point3D(f_point.x+160,120-f_point.y,0)
		#print "real_point",real_point
		#print "relative_point",relative_point

		x_comp = relative_point.dot_prod(x_axis)
		y_comp = relative_point.dot_prod(y_axis)
		#print f_point,x_comp*scale,y_comp*scale
		return x_comp*scale,y_comp*scale


	def contains(self,p):
		"Checks whether a point is in this plane"
		if abs(self.a * p.x + self.b * p.y + self.c * p.z - self.w) < .0000000001:
			return 1
		return 0


	def get_vector_pair(self):
		"Returns a pair of non-parallel vectors in the plane"
		p0 = self.vector_intersection(Point3D(0,0,1))
		p1 = self.vector_intersection(Point3D(1,0,1))
		p2 = self.vector_intersection(Point3D(0,1,1))
		#print p0,p1,p2
		return p1.subtract(p0),p2.subtract(p0)


	def get_point_normal(self):
		"Reeturns a point in the plane and a normal with a positive z value."
		p1,p2 = self.get_vector_pair()
		#print p1,p2	
		normal = p1.cross_prod(p2).normalize()
		#print normal
		if normal.z < 0:
			normal = normal.scalar_prod(-1)
		return self.vector_intersection(Point3D(0,0,1)),normal


	def vector_intersection(self,p,verbose=None):
		denom = (p.x * self.a + p.y * self.b + p.z * self.c)
		#print "denom",denom
		if denom != 0: 
			d = self.w/denom
		else:
			return None
		return Point3D(p.x * d, p.y * d, p.z * d)


	def move_plane(self, distance):
		z_intersect = self.vector_intersection(Point3D(0,0,1))
		#print z_intersect
		self_point, self_normal = self.get_point_normal()
		#print "Got normals for moving"
		#print pl_normal
		moved_plane_point = z_intersect.add(self_normal.scalar_prod(distance))
		return Plane3D.from_normal(moved_plane_point, self_normal)


	def __str__(self):
		return "Plane("+str(self.a)+","+str(self.b)+","+str(self.c)+","+str(self.w)+")"
		

def generate_origin_points_citrus():
	# point1 acts as the origin anchor (not the origin) and point2 acts to define the x-axis. The x-axis is the vector from point1 to point2
	origin_anchor_x_offset = -2
	origin_anchor_y_offset = -3
	simulation_height = 20 # height of the simulation video as the simulation y is inverted.
	s1 = Point3D(7.9,9.1,0) # The position in the simulation of point1 -- an identifiable point in the video.
	z =  599.91699576582687
	frame1 = Point3D(-79,7,z) # The position of point1 in the video
	frame2 = Point3D(63,40,z) # The position of point2 in the video
	#g = Plane3D(-0.11969563077067485, -2.3780598126031101, 1.9896041114414202, 55.0) # The ground plane
	#f = Plane3D(0,0,1,609.57054944472065) # The video frame plane
	g = Plane3D(-0.11928897007633524, -2.3366072925086012, 1.9896041114414202, 54.30710241772082)
	#g = Plane3D(0,1,1,1)
	


	p1 = Point3D(1,1,0)
	p2 = Point3D(0,0,1)
	#print p2.cross_prod(p1)
	#return
	#print "-----------------------"
	f = Plane3D(0,0,1,z)
	#print "g",g
	g_point,g_normal = g.get_point_normal()
	print "g decomp",g_point,g_normal,"----------"
	g_temp2 = Plane3D.from_normal(g_point,g_normal)
	#print "g_temp2",g_temp2
	t1,t2 = g_temp2.get_point_normal()
	#print "g_temp2 decomp",t1,t2
	g_temp = g.move_plane(0)
	#print "g_temp",g_temp
	t1,t2 = g_temp.get_point_normal()
	#print "g_temp decomp" ,t1,t2
	f_point,f_normal =  f.get_point_normal()

	frame4 = Point3D(-89,78,z)
	ground4 = g.vector_intersection(frame4)
	frame5 = Point3D(-82,52,z)
	pole_top_plane = g.move_plane(-2.5654)
	pole_top_plane5 = pole_top_plane.vector_intersection(frame5)
	ground6 = g_normal.scalar_prod(2.5654).add(pole_top_plane5)
	
	temp = Point3D(s1.x,simulation_height - s1.y,s1.z)
	s3 = temp.add(Point3D(origin_anchor_x_offset,origin_anchor_y_offset,0))
	ground1 = g.vector_intersection(frame1,1)
	print "Anchor",ground1
	ground2 = g.vector_intersection(frame2,1)
	print "X-axis point",ground2
	print "Distances",ground1.distance(ground2),ground1.distance(ground4),ground2.distance(ground4)
	print "Distance between pole top and pole bottom ", ground1.distance(pole_top_plane5)
	print "Distance between dropped and actual pole bottom ", ground1.distance(ground6)
	print "If one eye ",g.vector_intersection(frame5)
	ground_x = ground2.subtract(ground1).normalize()
	print ground_x
	print "g_normal",g_normal
	ground_y = g_normal.scalar_prod(1).cross_prod(ground_x).normalize()
	print ground_y
	ground3 = ground1.add(ground_x.scalar_prod(origin_anchor_x_offset)).add(ground_y.scalar_prod(origin_anchor_y_offset))
	print ground3
	ground4 = ground3.add(ground_x.scalar_prod(6))
	frame3 = f.vector_intersection(ground3)
	frame4 = f.vector_intersection(ground4)

	print "(po_x,po_y,p2_x,p2_y,wox,woy)\n" + "\t," + str(frame3.x) + "\t," + str(frame3.y) + "\t," + str(frame4.x) + "\t," + str(frame4.y) + "\t," + str(s3.x) + "\t," + str(s3.y)
	print "(po_x,po_y,p2_x,p2_y) w.r.t left-top origin\n" + "\t," + str(frame3.x + 160) + "\t," + str(120-frame3.y) + "\t," + str(frame4.x+160) + "\t," + str(120-frame4.y)
		
# given paths from a simulation with generated points along with other needed stuff from process_simmt, returns
# paths without generated points that correspond to motion on the screen.
def project_to_screen(paths,expr,g):
	fo = Point3D(expr.po_x,expr.po_y,0)
	f2 = Point3D(expr.p2_x,expr.p2_y,0)
	wo = Point3D(expr.wo_x,expr.wo_y,0)
	width = expr.video_width
	height = expr.video_height
	z = expr.z
	f = Plane3D.from_normal(Point3D(0,0,z),Point3D(0,0,1))
	g_point,g_normal = g.get_point_normal()
	fo.z = z
	f2.z = z
	go = g.vector_intersection(fo)
	g2	= g.vector_intersection(f2)
	x_axis = g2.subtract(go).normalize()
	y_axis = g_normal.cross_prod(x_axis).normalize()
	go_new = go.add(x_axis.scalar_prod(-wo.x)).add(y_axis.scalar_prod(-wo.y))
	projected_paths = []
	for path in paths:
		projected_path = Path(path.pointId,expr)
		for point in path.data:
			x,y,time = point['x'],point['y'],point['t']
			y = expr.simulation_height-y
			gp1 = go_new.add(x_axis.scalar_prod(x)).add(y_axis.scalar_prod(y))
			fp1 = f.vector_intersection(gp1)
			hbound = width/2
			vbound = height/2
			if hbound > fp1.x >= -hbound and vbound > fp1.y >= -vbound:
				projected_path.addPoint(fp1.x+hbound,height-(fp1.y+vbound),time)
		projected_paths.append(projected_path)
	return projected_paths
		
#-------------------------------------------------------------------
""" XML processing functions """

def get_a_document(name):
	return xml.dom.minidom.parse(name)

def count_text_nodes(root):
	count = 0
	if root.nodeType == 3:
		print root.nodeName
		count += 1
	for child in root.childNodes:
		count += count_text_nodes(child)
	return count
	
def count_all_nodes(root):
	count = 0
	count += 1
	for child in root.childNodes:
		count += count_all_nodes(child)
	return count
	
	
def delete_text_nodes(root):
	count = 0
	deleteList = []
	for child in root.childNodes:
		if child.nodeType == 3:
			deleteList.append(child)
		else:
			count += delete_text_nodes(child)
	count += len(deleteList)
	for tbd in deleteList:
		root.removeChild(tbd)
	return count
	
def delete_interpolation_nodes(root):
	count = 0
	passCount = 0
	deleteList = []
	for frameSet in root.getElementsByTagName("FRAME_SET"):
		for frame in frameSet.getElementsByTagName("FRAME"):
			deleteList = []
			for point in frame.getElementsByTagName("POINT"):
				if point.getElementsByTagName("STATUS")[0].childNodes[0].nodeValue == "INTERPOLATED":
					deleteList.append(point)
				else:
					passCount += 1
			count += len(deleteList)
			for tbd in deleteList:
				frame.removeChild(tbd)
	return count, passCount

def wrapper() :
	return 0

class Path:
	
	def __init__(self,pointId,expr = None):
		self.posns = []
		self.pointId = pointId
		self.data = []
		self.expr = expr
		self.haveGeneratedData = False
		if expr == None:
			print "Path being created with no experiment."
	
	def addPoint(self,x,y,time):
		self.posns.append({'x':x,'y':y,'time':time});
	
	def generatePoints(self,start,end,unit=1):
		if self.haveGeneratedData: return
		self.data = []
		done = False#(len(self.posns) == 0)
		pindex = 0
		ctime = start
		datai = 0
		while not done:
			
			if(pindex >= len(self.posns) - 1):
				"""no more points. only intrapolation for now"""
				done = True
				break
			if(ctime < self.posns[pindex]['time']):
				"""ctime is too behind"""
				ctime += unit
				continue
			if(self.posns[pindex + 1]['time'] < ctime):
				"""The next point is earlier than ctime. Need to move forward in our data"""
				pindex += 1
				continue
			x1=self.posns[pindex]['x']
			x2=self.posns[pindex+1]['x']
			y1=self.posns[pindex]['y']
			y2=self.posns[pindex+1]['y']
			t1=self.posns[pindex]['time']
			t2=self.posns[pindex+1]['time']
			try:
				xp = x1 + (x2 - x1) * 1. / (t2 - t1) * (ctime - t1)
				yp = y1 + (y2 - y1) * 1. / (t2 - t1) * (ctime - t1)
				xvp = (x2 - x1) * 1. / (t2 - t1)
				yvp = (y2 - y1) * 1. / (t2 - t1)
			except ZeroDivisionError:
				print "t1,t2 = ", t2,t1
				traceback.print_exc(file=sys.stdout)
				
			#print pindex, ctime,":", x1, y1,t1,",", x2, y2, t2,",",xp,yp
			self.data.append({'t':ctime,'x':xp,'y':yp,'xv':xvp,'yv':yvp});
			ctime += unit
			
		self.smoothPointsWithCosineCurve(start,end,unit)
		self.haveGeneratedData = True
		return
		
	def smoothPointsWithCosineCurve(self,start,end,unit):
		if self.expr.cosine_bell_smoothing_pieces == 0 or len(self.data) <= 1: return;
		x = []
		y = []
		t = []
		for dpoint in self.data:
			x.append(dpoint['x'])
			y.append(dpoint['y'])
			t.append(dpoint['t'])
		xnew = cbs(x,self.expr.cosine_bell_smoothing_pieces)
		ynew = cbs(y,self.expr.cosine_bell_smoothing_pieces)
		xv = []
		yv = []
		for ti in range(0,len(t) - 1):
			xv.append((xnew[ti+1] - xnew[ti]) *1. / (t[ti+1] - t[ti]))
			yv.append((ynew[ti+1] - ynew[ti]) *1. / (t[ti+1] - t[ti]))
		xv.append(xv[len(xv)-1])
		yv.append(yv[len(yv)-1])
		newdata = []
		for ti in range(0,len(t)):
			newdata.append({'t':t[ti],'x':xnew[ti],'y':ynew[ti],'xv':xv[ti],'yv':yv[ti]})
		self.data = newdata
		
	def getPoint(self,t):
		for entry in self.data:
			if(entry['t'] == t):
				return [entry['x'], entry['y']]
		return [None,None]
			
	def getVelocity(self,t):
		for entry in self.data:
			if(entry['t'] == t):
				return [entry['xv'], entry['yv']]
		return [None,None]
		
	def __str__(self):
		lines = []
		lines.append("####################################################")
		lines.append("point id = " + str(self.pointId))
		lines.append(str(self.pointId))
		for entry in self.posns:
			lines.append(str(entry))
		lines.append("------------------------------------")
		for entry in self.data:
			lines.append(str(entry) + " v = " + str(sqrt(entry['xv']**2 + entry['yv']**2)))
		return "\n".join(lines)
		
		
	def cropAndScale(self,expr,originx, originy,scale,invertyheight=None,invertyheight2=None):
		newPath = Path(self.pointId,expr)
		for entry in self.posns:
			if invertyheight:
				newPath.addPoint((entry['x'] - originx) * scale,invertyheight2 - ((invertyheight - entry['y'] - originy)* scale),entry['time'])
			else:
				newPath.addPoint((entry['x'] - originx) * scale,(entry['y'] - originy)* scale,entry['time'])
		return newPath

""" cosine bell smoothing """
def cbs(array,n):
    if len(array) == 0: return array
    new_array = [0] * len(array)
    limit = int(math.floor(n / 2.))
    for root in range(0,len(array)):
        factor_sum = 0
        new_value = 0
        for offset in range(-limit,limit+1):
            if(0 <= root+offset < len(array) and 0 <= root-offset < len(array)):
                factor = math.cos(math.pi / n * offset)
                factor_sum += factor
                new_value += array[root+offset] * factor
        new_array[root] = new_value / factor_sum
    return new_array		
		
def test():
	p = Path(0)
	p.addPoint(0,0,0);
	p.addPoint(10,1,10);
	p.addPoint(2,0,20);
	p.generatePoints(0,20,1);
	for dic in p.data:
		print  dic;
	return p
	

	

#-------------------------------------------------------------------
""" XML access functions """

def getPointCount(xml):
	if(xml.getElementsByTagName("MAX_POINT_ID")[0].childNodes.length != 1):
		print "Error in MAX_POINT_ID"
	return int(xml.getElementsByTagName("MAX_POINT_ID")[0].childNodes[0].nodeValue)

def getNumberedFrames(xml):
	frames = [None] * (xml.getElementsByTagName("FRAME_SET")[0].getElementsByTagName("FRAME").length)
	for frame in xml.getElementsByTagName("FRAME_SET")[0].getElementsByTagName("FRAME"):
		frameId = frame.getElementsByTagName("F_ID")[0].childNodes[0].nodeValue
		frames[int(frameId)] = frame
	return frames

def getNumberedPoints(frame, maxPointID):
	points = [None] * (maxPointID)
	for point in frame.getElementsByTagName("POINT"):
		pointId = point.getElementsByTagName("P_ID")[0].childNodes[0].nodeValue
		points[int(pointId)] = point
	return points
	
def getPointValue(point,dimension):
	return int(point.getElementsByTagName(dimension)[0].childNodes[0].nodeValue)

#The returned file is 
def xmlToPaths(xml,expr,scale=1):
	if expr.have_plane:
		plane = Plane3D(expr.a,expr.b,expr.c,expr.w).move_plane(-(expr.people_height))
	if expr.have_sim_anchor:
		po=Point3D(expr.po_x,expr.po_y,0)
		p2=Point3D(expr.p2_x,expr.p2_y,0)
	pointCount = getPointCount(xml)
	paths = [0] * (pointCount)
	for i in range(pointCount):
		paths[i] = Path(i,expr);
	frames = getNumberedFrames(xml)
	for frameId in range(len(frames)):
		points = getNumberedPoints(frames[frameId],pointCount)
		for pointId in range(pointCount):
			if(points[pointId]):
				x_old,y_old = getPointValue(points[pointId],"X"),getPointValue(points[pointId],"Y")
				if expr.have_plane:
					if expr.have_sim_anchor:
						x,y = plane.get_2d_coordinates_on_w(plane,expr.z,Point3D(x_old - expr.video_width/2,expr.video_height/2 - y_old,0),po,p2,scale)
					else:
						x,y = plane.get_2d_coordinates_on_w(plane,expr.z, Point3D(x_old - expr.video_width/2,expr.video_height/2 - y_old,0))
					#DEBUG
					#print x,y,x_old - width/2,y_old - height/2
				else:
					x,y = x_old,y_old
				#if pointId == 2:
					#print x_old,y_old, x,y
				if expr.have_plane:
					paths[pointId].addPoint(x,expr.viewport_height-y,frameId)
				else:
					paths[pointId].addPoint(x,y,frameId)
	return paths
	
#-------------------------------------------------------------------
"""CSV Access functions """

def getPathsInLogFile(file,expr,fps=30):
	paths = []
	lastframeforid = {}
	columns = 18
	#print file
	try:
		f = open(file,'r')
	except IOError:
		print "Some Error:"	,file + " could not be opened. Create one with each line as follows\n<Name>, <start>, <end>, <manualProcessedFile>, <opticalFlowProcessedFile>\n"
		return paths
	f.readline()
	for line in f:
		words = re.split(",\s*",line[:-1])
		if len(words) == columns:
			#print words[1], fps
			frameId = int(float(words[1]) * fps)
			iD = int(words[3])
			if words[5] != "NaN" and words[6] != "NaN":
				x = float(words[5])
				y = float(words[6])
				while iD >= len(paths):
					paths.append(Path(len(paths),expr))
				if not lastframeforid.has_key(iD) or lastframeforid[iD] < frameId:
					paths[iD].addPoint(x,y,frameId)
					lastframeforid[iD] = frameId
		else:
			if len(words) == 4:
				frameId = int(float(words[0]) * fps)
				iD = int(words[1])
				x = float(words[2])
				y = float(words[3])
				while iD >= len(paths):
					paths.append(Path(len(paths),expr))
				if not lastframeforid.has_key(iD) or lastframeforid[iD] < frameId:
					paths[iD].addPoint(x,y,frameId)
					lastframeforid[iD] = frameId
			else:
				print "contents of " + file + " is in the wrong format. " + str(columns) + " or 4 columns expected. " + str(len(words)) + " found"
				return paths
	return paths

def savePaths(paths, start,end,step,file="paths.txt"):
	f = open(file,'w')
	for path in paths:
		f.write(str(path))
		f.write("\n")
	f.close()

def savePathsCVS(paths, start,end,step,file="paths.cvs"):
	f = open(file,'w')
	for id in range(0,len(paths)):
		path = paths[id]
		#f.write("------------------------\n")
		for i in range(0,len(path.data)):
			entry = path.data[i]
			f.write(str(path.pointId)+"\t,"+str(entry['t'])+"\t,"+str(entry['x'])+"\t,"+str(entry['y']))
			f.write("\n")
	f.close()
	
# Function to read a CVS file to a two dimensional array of strings.
def cvs_read_clip_write_array(infile, outfile, start_time, duration,segments = 1,delay = 0):
	lines = []
	f = None
	fout = None
	segment = 0
	f = open(infile,'r')
	header = f.readline()
	
	for line in f:
		words = re.split(",\s*",line[:-1])
		#print words
		if not fout and float(words[1]) > start_time:
			try:
				fout = open(outfile+""+str(segment)+".csv",'w')
				fout.write(header)
			except IOError,inst:
				print "IOError:",str(inst)
				break				
		if float(words[1]) >= start_time and float(words[1]) <= start_time+duration:
			words[1] = str(float(words[1]) - start_time)
			for i in range(len(words)):
				fout.write(words[i])
				if i < len(words) - 1:
					fout.write(",")
				else:
					fout.write('\n')
		if float(words[1]) > start_time+duration:
			if  segment >= segments-1:
				break
			else:
				fout.close();
				fout = None
				start_time += delay;
				segment += 1
	f.close()
	if fout: 
		fout.close()

#-------------------------------------------------------------------
""" plotting stuff  """

def getPathPoints(path,start,end,index):
	vs = []
	for i in range(start,end+1):
		point = path.getPoint(i)
		if(point[0] != None and point[1] != None):
			vs.append(point[index])
	return  vs
	
	
def plotPath(path,start,end):
	xs = getPathPoints(path,start,end,0)
	"print xs"
	ys = getPathPoints(path,start,end,1)
	"print xs"
	pylab.plot(xs,ys)
	pylab.axis([0,320,0,240])
	pylab.show()
	
"""
def test(i):
	paths = xmlToPaths(x)
	path = paths[i]
	path.generatePoints(0,100)
	plotPath(path,0,100)
"""	
def plotPaths(paths,expr):
	start=expr.start
	end=expr.end
	step=expr.step
	width=expr.width
	height=expr.height
#	pylab.hold(True)
	for index in range(len(paths)):
		path = paths[index]
		path.generatePoints(start,end,step)
		xs = getPathPoints(path,start,end,0)
		ys = getPathPoints(path,start,end,1)
		ys = [height-y for y in ys]
		line, = pylab.plot(xs,ys)
		line.set_linestyle('-')
		line.set_marker('.')
		pylab.plot(xs[:1],ys[:1],'go')
		pylab.plot(xs[-1:],ys[-1:],'ro')
	#pylab.show()
	
def plotVectors(conData, expr, scale=1,color='black'):
	#print conData
	start=expr.start
	end=expr.end
	step=expr.step
	width=expr.width
	height=expr.height
	gridw = expr.grid_width
	gridh = expr.grid_height
	cellx = width / gridw
	celly = height / gridh
	for x in range(gridw):
		for y in range(gridh):
			index = y * gridw + x
			xoff = cellx/2 + cellx*x
			yoff = celly/2 + celly*y
			pylab.plot([xoff],[height-yoff],"wo")
			line, = pylab.plot([xoff, xoff + conData[index][0]*scale],[height -yoff, height - (yoff + conData[index][1]*scale)])
			line.set_linestyle('-')
			line.set_linewidth(2.0)
			line.set_color(color)
	pylab.plot([cellx/2],[height-celly/2],"ro")

#----------------------------------------------
# statistical functions

"Return N, mean,dev"
def stats_describe(array):
	mean = 0
	count = 0
	for v in array:
		if v:
			count += 1
			mean += v
	if count == 0:
		return 0,None,None
	if count == 1:
		return 1,mean,0
	mean = mean * 1.0 / count
	dev = 0
	for v in array:
		if v:
			dev += (v - mean) ** 2
	return count,mean, sqrt(dev * 1.0 / (count-1))
			
			
# -------------------------------------------
# Path analysis functions.

def get_all_velocity(paths, start, end, step):
	alldata = []
	for path in paths:
		data = [ path.getVelocity(t) for t in range(start, end,step) ]
		alldata.extend(data)
	return alldata
	
def get_velocity_per_frame(paths, start,end,step):
	frameData = [None] * end
	for i in range(len(frameData)):
		frameData[i] = []	
	for path in paths:
		for t in range(start,end,step):
			frameData[t].append(get_magnitude(path.getVelocity(t)))
	return frameData
	
def get_magnitude(v):
	if v[0] != None and v[1] != None:
		return sqrt(v[0] ** 2 + v[1] ** 2)
	else:
		return None
		
def get_velocity_per_person(paths, start,end,step):
	idData = []
	for id in  range(len(paths)):
		idData.append([])
	for id in range(len(paths)):
		for t in range(start,end,step):
			idData[id].append(get_magnitude(paths[id].getVelocity(t)))
	return idData


"return max velocity, "
def get_max_velocity(velocities):
	max = 0
	for v in velocities:
		if v != None and v > max:
				max = v
	return max

def get_magnitudes(velocities):
	vs = []
	for v in velocities:
		if v[0] != None and v[1] != None:
			vs.append(sqrt(v[0] ** 2 + v[1] ** 2))
	return vs
	

# Distance calculation functions
def get_distance(p1,p2):
	return sqrt((p1[0] - p2[0])**2 + (p1[1] - p2[1])**2)
	
def get_closest_distance(paths,myid,time):
	gotDistance = None
	distance = 100000000
	loc = paths[myid].getPoint(time)
	
	#if myid == 1:
		#print "loc", loc
	if not loc[0] or not loc[1]:
		return None
	for id in range(len(paths)):
		if not myid == id:
			nloc = paths[id].getPoint(time)
			if nloc[0] and nloc[1]:
				d = get_distance(nloc,loc)
				if d < distance:
					distance = d
					gotDistance = 1
	if not gotDistance:
		return None
	return distance

def get_all_distance(paths,start,stop,step):
	alldata = []
	for id in range(len(paths)):
		data = []
		for time in range(start,stop,step):
			data.append(get_closest_distance(paths,id,time))
		alldata.extend(data)
	return alldata		
	
def get_distance_per_person(paths,start,stop,step):
	alldata = []
	for id in range(len(paths)):
		data = []
		for time in range(start,stop,step):
			data.append(get_closest_distance(paths,id,time))
		alldata.append(data)
	return alldata
	
	
# Histogram functions
def pa_mean_compare(md1,md2):
	return cmp(md1[0],md2[0])

" Saves the velocity statistics in a file. The mean and deviation velocities of each person is calculated. \
They are sorted by the mean and written into the file"
def save_velocity_data_pp(data,name,experiment,scale=1):
	f = open(experiment+"_"+name+"_vel_pp.txt",'w')
	#f.write("person "+experiment+"_"+name+"_velocity")
	mean_dev = []
	for id in range(len(data)):
		#print data[id]
		n,mean,dev = stats_describe(data[id])
		#print id, n,mean,dev
		if mean and dev:
			mean_dev.append([mean * scale,dev * scale])
	
	mean_dev.sort(cmp=pa_mean_compare)
	#print mean_dev
	size = len(mean_dev)
	for i in range(size):
		if mean_dev[i][0]:
			f.write(str(i * 1.0 / size) + "\t"+ str(mean_dev[i][0])+"\t"+ str(mean_dev[i][1])+"\n")
	f.close()
	
" Saves the distance statistics in a file. The mean and deviation velocities of each person is calculated. \
They are sorted by the mean and written into the file"
def save_distance_data_pp(data,name,experiment,scale=1):
	f = open(experiment+"_"+name+"_dist_pp.txt",'w')
	#f.write("person "+experiment+"_"+name+"_velocity")
	mean_dev = []
	for id in range(len(data)):
		#print data[id]
		n,mean,dev = stats_describe(data[id])
		#print id, n,mean,dev
		if mean and dev:
			mean_dev.append([mean * scale,dev * scale])
			
	mean_dev.sort(cmp=pa_mean_compare)
	#print mean_dev
	size = len(mean_dev)
	for i in range(size):
		if mean_dev[i][0]:
			f.write(str(i * 1.0 / size) + "\t"+ str(mean_dev[i][0])+"\t"+ str(mean_dev[i][1])+"\n")
	f.close()

""
def save_velocity_graph_pp(mtdata,simdata,experiment,pixelsperm=1.):
	save_velocity_data_pp(simdata,"Sim",experiment)
	save_velocity_data_pp(mtdata,"MT",experiment,1./pixelsperm)
	f = open(experiment+"_vel_pp.plt",'w')
	f.write("\
set auto x\n\
set auto y\n\
set xlabel \"People (Sorted on Y)\"\n\
set ylabel \"Average velocity (meters per frame)\"\n\
set title \"Average velocity per person with error bars\" \n\
set style data histogram\n\
set style fill solid border -1\n\
plot \""+ experiment+"_MT_vel_pp.txt\" with errorbars, \""+ experiment+"_Sim_vel_pp.txt\" with errorbars\n\
pause mouse\n\
")
	
""
def save_distance_graph_pp(mtdata,simdata,experiment,pixelsperm=1.):
	save_distance_data_pp(simdata,"Sim",experiment)
	save_distance_data_pp(mtdata,"MT",experiment,1./pixelsperm)
	f = open(experiment+"_dist_pp.plt",'w')
	f.write("\
set auto x\n\
set auto y\n\
set xlabel \"People (Sorted on Y)\"\n\
set ylabel \"Distance (meters)\"\n\
set title \"Interpersonal distance per person with error bars\" \n\
set style data histogram\n\
set style fill solid border -1\n\
plot \""+ experiment+"_MT_dist_pp.txt\" with errorbars, \""+ experiment+"_Sim_dist_pp.txt\" with errorbars\n\
pause mouse\n\
")

" Creates a txt file and a plt file to render a histogram of mtdata and simdata. The velocity of each person in each step is broken\
into a histogram "
def save_velocity_histogram(mtdata, simdata, experiment,step,pixelsperm=1.):
	f = open(experiment+"_vel.txt",'w')
	"Convert velocity to its magnitude"
	mtv = get_magnitudes(mtdata)
	simv = get_magnitudes(simdata)
	#print mtv[1:100]
	for i in range(len(mtv)):
		mtv[i] /= pixelsperm
	#print mtv[1:100]
	vlimit = max(max(mtv), max(simv))
	"Make buckets"
	mt_freq = [0] * (int(vlimit/step) + 1)
	sim_freq = [0] * (int(vlimit/step)+1)
	"Count frequency"
	for v in mtv:
			mt_freq[int(v / step)] += 1
	for v in simv:
			sim_freq[int(v / step)] += 1
	"Get upper limit for graph"
	fmax = max(max(mt_freq),max(sim_freq))
	n, mtv_mean, mtv_dev = stats_describe(mtv)
	n, simv_mean, simv_dev = stats_describe(simv)
	f.write("velocity MT_freq(Mean="+str(mtv_mean)+"_dev="+str(mtv_dev)+") Sim_freq(Mean="+str(simv_mean)+"_Dev="+str(simv_dev)+")\n")
	for v in range(len(mt_freq)):
		f.write(str(v * step)+"\t"+str(mt_freq[v])+"\t"+str(sim_freq[v])+"\n")
	f.close()
	f = open(experiment+"_vel.plt",'w')
	f.write("\
set auto x\n\
set xlabel \"Velocity (meters per frame)\"\n\
set ylabel \"Frequency\"\n\
set yrange [0:"+str(fmax+2)+"]\n\
set style data histogram\n\
set style fill solid border -1\n\
plot \""+experiment+"_vel.txt\" using 2:xtic(1) ti col, '' u 3 ti col\n\
pause mouse\n\
")
	f.close()
	
" Creates a txt file and a plt file to render a histogram of mtdata and simdata. The velocity of each person in each step is broken\
into a histogram "
def save_distance_histogram(mtdata, simdata, experiment,step,pixelsperm=1.):
	f = open(experiment+"_dist.txt",'w')
	"Convert velocity to its magnitude"
	mtd = mtdata
	simd = simdata
	for i in range(len(mtd)):
		if mtd[i]:
			mtd[i] /= pixelsperm
	dlimit = max(max(mtd), max(simd))
	"Make buckets"
	mt_freq = [0] * (int(dlimit/step) + 1)
	sim_freq = [0] * (int(dlimit/step)+1)
	"Count frequency"
	for d in mtd:
			if d:
				mt_freq[int(d / step)] += 1
	for d in simd:
			if d:
				sim_freq[int(d / step)] += 1
	"Get upper limit for graph"
	fmax = max(max(mt_freq),max(sim_freq))
	n, mtd_mean, mtd_dev = stats_describe(mtd)
	n, simd_mean, simd_dev = stats_describe(simd)
	f.write("distance MT_freq(Mean="+str(mtd_mean)+"_dev="+str(mtd_dev)+") Sim_freq(Mean="+str(simd_mean)+"_Dev="+str(simd_dev)+")\n")
	for d in range(len(mt_freq)):
		f.write(str(d * step)+"\t"+str(mt_freq[d])+"\t"+str(sim_freq[d])+"\n")
	f.close()
	f = open(experiment+"_dist.plt",'w')
	f.write("\
set auto x\n\
set xlabel \"Distance (meters)\"\n\
set ylabel \"Frequency\"\n\
set yrange [0:"+str(fmax+2)+"]\n\
set style data histogram\n\
set style fill solid border -1\n\
plot \""+experiment+"_dist.txt\" using 2:xtic(1) ti col, '' u 3 ti col\n\
pause mouse\n\
")
	f.close()
	
def get_velocity_stats(velocities):
	mx = 0
	my = 0
	err = 0
	count = 0
	for v in velocities:
		if v[0] != None and v[1] != None:
			mx += v[0]
			my += v[1]
			count += 1
		
	mx /= count
	my /= count
	
	for v in velocities:
		if v[0] != None and v[1] != None:
			err += (mx - v[0]) ** 2 + (my - v[1]) ** 2
	
	return [mx, my], sqrt(err)

	
		
#-------------------------------------------------------------------
""" vector correlation using complex numbers """

def cn_add(x,y):
	return cn_add_to([x[0],x[1]],y)

def cn_add_to(x,y):
	x[0] += y[0]
	x[1] += y[1]
	return x

def cn_sub(x,y):
	return cn_sub_fr([x[0],x[1]],y)

def cn_conj(x):
	return cn_conj_this([x[0],x[1]])

def cn_conj_this(x):
	x[1] = -x[1]
	return x

def cn_sub_fr(x,y):
	x[0] -= y[0]
	x[1] -= y[1]
	return x

def cn_mul_sc(x,y):
	return cn_mul_sc_to([x[0],x[1]],y)

def cn_mul_sc_to(x,y):
		x[0] *= y
		x[1] *= y
		return x
	
def cn_mul_vec(x,y):
	return[x[0] * y[0] - x[1]*y[1], x[0]*y[1] + x[1]*y[0]]
	
def cn_mean(xl):
	mean = [0.,0.]
	for x in xl:
		mean = cn_add(mean,x)
	return cn_mul_sc_to(mean,1./len(xl))

def cn_var(xl,mean=None):
	if not mean:
		mean = cn_mean(xl)
	sum = [0.,0.]
	for x in xl:
		xdiff = cn_sub(x,mean)
		cn_add_to(sum,cn_mul_vec(cn_conj(xdiff),xdiff))
	cn_mul_sc_to(sum,1./len(xl))
	return sum
	
def cn_covar(xl,yl,mean_x=None,mean_y=None):
	if not mean_x:
		mean_x = cn_mean(xl)
	if not mean_y:
		mean_y = cn_mean(yl)
	sum = [0.,0.]
	for i in range(len(xl)):
		xdiff = cn_sub(xl[i],mean_x)
		ydiff = cn_sub(yl[i],mean_y)
		cn_add_to(sum,cn_mul_vec(cn_conj(xdiff),ydiff))
	cn_mul_sc_to(sum,1./len(xl))
	return sum
	
def cn_corr(xl,yl,mean_x=None,mean_y=None):
	"Calculated correlation between two list of vectors. Vectors have to be \
2 dimensional and are represented by a list of two floats. E.g. [[0,0],[1,1],....]"
	if not mean_x:
		mean_x = cn_mean(xl)
	if not mean_y:
		mean_y = cn_mean(yl)
	corr = cn_covar(xl,yl,mean_x,mean_y)
	vx = pow( cn_var(xl,mean_x)[0], .5)
	vy = pow( cn_var(yl,mean_y)[0], .5)
	cn_mul_sc_to(corr, 1./ (vx * vy) )
	slope = cn_mul_sc(corr, vy/vx)
	offset = cn_sub(mean_y, cn_mul_vec(slope,mean_x))
	return corr, slope, offset
	
#cn_xl = [[0,1],[1,2],[1,3]]
#cn_yl = [[0,1],[1,2],[1,3]]


# -------------------------------------------
# Functions for converting paths to velocity and flux

def printPathAnalysis(paths,expr,outPut="data.txt"):
	"Returns a two dimensional array of 3-tuple. Each tuple containing a velocity/flux (x,y) and the number of people in the cell."
	"This function includes the end frame in the result. The end frame data is a duplicate of the previous frame data"
	#print gridw,gridh,width,height
	
	fluxor = HumanPositionToFluxConverter.make(expr.flux_converter_name)
	fluxor.setGridSize(int(expr.cell_width),int(expr.cell_height))
	frameData = [None] * (expr.end+1)
	for frame in range(expr.start,expr.end+1,expr.step): #range(gridx * gridy):
		frameData[frame] = [None] * (expr.grid_width*expr.grid_height)
		for grid in range(expr.grid_width*expr.grid_height):
			frameData[frame][grid] = [0] * 3
	for frame in range(expr.start,expr.end+1,expr.step):
		for path in paths:
			point = path.getPoint(frame)
			velocity = path.getVelocity(frame)
			#print point 
			#print "\n"
			if(point[0] != None and point[1] != None and 0 <= point[0] < expr.width and 0<=point[1]<expr.height):
				#print str(point[0]) + "\t" + str(point[1]) + "\t" + str(gridx) + " " + str(gridy) + " " + str(gridw* gridy + gridx) + "\n"
				if 0:
					gridx = int((point[0]) * 1. * expr.grid_width / width)
					gridy = int((point[1]) * 1. * expr.grid_height/ height)
					frameData[frame][expr.grid_width*gridy + gridx][0] += velocity[0]
					frameData[frame][expr.grid_width*gridy + gridx][1] += velocity[1]
					frameData[frame][expr.grid_width*gridy + gridx][2] += 1
				else:
					for grid_x in range(0,expr.grid_width):
						for grid_y in range(0,expr.grid_height):
							coeff = fluxor.getCoefficient(point[0],point[1],grid_x * (expr.width/expr.grid_width),grid_y *(expr.height/expr.grid_height))
							frameData[frame][expr.grid_width * grid_y + grid_x][0] += velocity[0] * coeff
							frameData[frame][expr.grid_width * grid_y + grid_x][1] += velocity[1] * coeff
							frameData[frame][expr.grid_width * grid_y + grid_x][2] += coeff
	if outPut:
		out = open(outPut,'w')
		for frame in range(expr.start,expr.end+1,expr.step):
			for grid in range(expr.grid_width*expr.grid_height):
				out.write("" \
				+ str(frame) + "," \
				+ str(grid) + "," \
				+ str(frameData[frame][grid][0]) + "," \
				+ str(frameData[frame][grid][1]) + ",")
				if(frameData[frame][grid][2] > 0):
					out.write("" \
					+ str(frameData[frame][grid][0] * 1. / frameData[frame][grid][2]) + "," \
					+ str(frameData[frame][grid][1] * 1. / frameData[frame][grid][2]) + ",")
				else:
					out.write("0,0,")
				out.write(str(frameData[frame][grid][2]) + "\n")
		out.close()
	return frameData
			
			
def calculateValues(frameData, expr):
	conData = [0] * len(frameData[expr.start])
	frames = 0.
	for frame in range(expr.start,expr.end+1,expr.step):
		for grid in range(len(frameData[frame])):
			if(conData[grid] == 0):
				conData[grid] = [0] * 3
			if(expr.flux_type == 'average'):
				if(frameData[frame][grid][2] > 0):
					conData[grid][0] += frameData[frame][grid][0] * 1. / frameData[frame][grid][2]
					conData[grid][1] += frameData[frame][grid][1] * 1. / frameData[frame][grid][2]
					conData[grid][2] += frameData[frame][grid][2]
			else:
				conData[grid][0] += frameData[frame][grid][0]
				conData[grid][1] += frameData[frame][grid][1]
				conData[grid][2] += frameData[frame][grid][2]
		frames += 1
	"""if(vtype == 'velocity'):
		for grid in range(len(frameData[start])):
			if(conData[grid][2] > 1):
				conData[grid][0] /= float(conData[grid][2]) 
				conData[grid][1] /= float(conData[grid][2])"""
	for grid in range(len(frameData[expr.start])):
		conData[grid][0] /= frames
		conData[grid][1] /= frames
	return conData
		
def process(source,expr,scale=1,showPath=True,showGraph=False,sourceType='file',outPut=None):
	#print width, height
	# remove manual tracker interpolation and produce custom interpolation
	paths = []
	#print source
	if sourceType == 'paths':
		#print "pathtype source found",width,height
		paths = source
	else:
		if re.match("^.*\.xml$",source):
			print "\tConverting XML to paths"
			xmlData = get_a_document(source)
			delete_interpolation_nodes(xmlData)
			paths = xmlToPaths(xmlData,expr)
		elif re.match("^.*\.csv$",source):
			paths=getPathsInLogFile(source,expr,fps=expr.video_fps)
		print "\tGenerating point in paths"
		for path in paths:
			path.generatePoints(start=expr.start,end=expr.end,unit=expr.step)
		
	
	#print "Created paths"
	savePaths(paths,expr.start,expr.end,expr.step)
	#print paths[len(paths)-20]
	print "\tAnalyzing paths"
	frameData = printPathAnalysis(paths,expr,outPut=outPut)
	#print frameData
	#print "Analyzed Paths"
	print "\tConsolidating"
	conData = calculateValues(frameData,expr)

	if(showGraph):
		if(showPath):
			plotPaths(paths,expr)
			#print "Plotted Paths"
		#print conData

		plotVectors(conData,expr,scale=scale)
		#print "Plotted vectors"

		#pylab.grid(True)
		pylab.axis([0,expr.width,0,expr.height])
		if isinstance(showGraph,str):
			pylab.savefig(expr.name+'_'+showGraph+'.pdf')
			pylab.savefig(expr.name+'_'+showGraph+'.png')
			pylab.clf()
		else:
			pylab.show()
	print "#########################################################"
	
	return frameData,conData


#-----------------------------------------------------------------
# GUI Stuff

class ManualTrackingInput:
	def __init__(self,master):
		self.frame = tk.Frame(master)
		self.frame.pack()
		
		self.fileNameL = tk.Label(self.frame,text='Input XmlFile name')
		self.fileNameL.pack(side=tk.TOP)
		self.fileName = tk.StringVar(self.frame,"manual.xml","fileName")
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
		
		self.startL = tk.Label(self.frame,text='start')
		self.startL.pack(side=tk.TOP)
		self.start = tk.IntVar(self.frame,"0","start")
		self.startE = tk.Entry(self.frame,textvariable=self.start)
		self.startE.pack(side=tk.TOP)
		
		self.endL = tk.Label(self.frame,text='end')
		self.endL.pack(side=tk.TOP)
		self.end = tk.IntVar(self.frame,"100","end")
		self.endE = tk.Entry(self.frame,textvariable=self.end)
		self.endE.pack(side=tk.TOP)
		
		self.stepL = tk.Label(self.frame,text='step')
		self.stepL.pack(side=tk.TOP)
		self.step = tk.IntVar(self.frame,"5","step")
		self.stepE = tk.Entry(self.frame,textvariable=self.step)
		self.stepE.pack(side=tk.TOP)


		self.fluxmodelL = tk.Label(self.frame,text='fluxmodel')
		self.fluxmodelL.pack(side=tk.TOP)
		self.fluxmodel = tk.StringVar(self.frame,"single_pixel","fluxmodel")
		self.fluxmodelE = tk.Entry(self.frame,textvariable=self.fluxmodel)
		self.fluxmodelE.pack(side=tk.TOP)
		
		self.fpsL = tk.Label(self.frame,text='FPS')
		self.fpsL.pack(side=tk.TOP)
		self.fps = tk.IntVar(self.frame,"25","fps")
		self.fpsE = tk.Entry(self.frame,textvariable=self.fps)
		self.fpsE.pack(side=tk.TOP)

		self.cbsL = tk.Label(self.frame,text='CBS Pieces')
		self.cbsL.pack(side=tk.TOP)
		self.cbs = tk.DoubleVar(self.frame,"0","cbs_pieces")
		self.cbsE = tk.Entry(self.frame,textvariable=self.cbs)
		self.cbsE.pack(side=tk.TOP)
		
		#		self.vtypeL = tk.Label(self.frame,text='vtype')
#		self.vtypeL.pack(side=tk.TOP)
		self.vtype = tk.StringVar(self.frame,"velocity",name="vtype")
#		self.vtypeE = tk.Entry(self.frame,textvariable=self.vtype)
#		self.vtypeE.pack(side=tk.TOP)
		
		self.vtypeVelRB = tk.Radiobutton(self.frame,text="Average Velocity   ",variable=self.vtype,value="velocity")
		self.vtypeVelRB.pack()
		self.vtypeVelRB = tk.Radiobutton(self.frame,text="Total Velocity        ",variable=self.vtype,value="total_velocity")
		self.vtypeVelRB.pack()
		
		self.showGraph = tk.StringVar(self.frame,"False","showGraph")
		self.showGraphCB = tk.Checkbutton(self.frame,text="Show Graph           ",offvalue="False",onvalue="True",variable=self.showGraph)
		self.showGraphCB.pack()

		self.showPath = tk.StringVar(self.frame,"True","showPath")
		self.showPathCB = tk.Checkbutton(self.frame,text="Show Path in Graph",offvalue="False",onvalue="True",variable=self.showPath)
		self.showPathCB.pack()
		
		self.AcceptB = tk.Button(self.frame,text='Process')
		self.AcceptB.pack(side=tk.TOP)
		self.AcceptB.bind("<Button-1>",self.doProcess)
	
	def doProcess(self,event):
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
		outFileName = self.fileName.get()+".csv"
			
		expr = Experiment()
		expr.name = self.fileName.get()
		expr.flux_converter_name = self.fluxmodel.get()
		expr.video_fps = self.fps.get()
		expr.video_width=self.width.get()
		expr.video_height=self.height.get()
		expr.width=self.width.get()
		expr.height=self.height.get()
		expr.grid_width=self.gridw.get()
		expr.grid_height=self.gridh.get()
		expr.cell_width = expr.width / expr.grid_width
		expr.cell_height = expr.height / expr.grid_height
		expr.start=self.start.get()
		expr.end=self.end.get()
		expr.step=self.step.get()
		expr.vtype=self.vtype.get()
		expr.cosine_bell_smoothing_pieces = self.cbs.get()
		
		try:
			process(source=self.fileName.get()\
			,expr=expr
			,scale=self.scale.get()
			,showPath=(self.showPath.get() != "False")\
			,showGraph=(self.showGraph.get() != "False")\
			,outPut=outFileName
			)
		except Exception, inst:
			traceback.print_exc(file=sys.stdout)
			tkMessageBox._show("Some Error"\
			,"The processing of the XML file could not be completed\n"+str(inst)\
			,icon=tkMessageBox.ERROR\
			,type=tkMessageBox.OK\
			,parent=self.frame)
			return
		tkMessageBox._show("Data Saved"\
		,"Data saved to "+os.path.abspath(outFileName)\
		,icon=tkMessageBox.INFO\
		,type=tkMessageBox.OK\
		,parent=self.frame)
		#self.AcceptB.configure(state=self.AcceptB.NORMAL)
		return
		
		
		
def showGui():
	
	"""tkMessageBox._show("Data Saved"\
	,"Data saved to "\
	,icon=tkMessageBox.INFO\
	,type=tkMessageBox.OK)
	"""
	root = tk.Tk()
	m = ManualTrackingInput(root)
	root.mainloop()
	
def all_meters_per_pixel():
	plane = Plane3D(-0.11969563077067485, -2.3780598126031101, 1.9896041114414202, 55.0)
	z = 609.570549
	f = open("all_mpp.csv","w")
	for x in range(-160,160):
		for y in range(120,-120,-1):
			base = plane.vector_intersection(Point3D(x,y,z))
			base_x = plane.vector_intersection(Point3D(x+.01,y,z))
			base_y = plane.vector_intersection(Point3D(x,y+0.01,z))
			xmeterspp = base.distance(base_x) * 100
			ymeterspp = base.distance(base_y) * 100
			f.write(str(x + 160)+","+str(120 - y)+","+str(xmeterspp)+","+str(ymeterspp)+"\n")
	f.close()
			
			
def test_flux():
	print "1"
	fluxor = HumanPositionToFluxConverter.make_debug_default()
	print "2"
	fluxor.setGridSize(50,50,10)
	print "3"
	print "Should be .1111 ", fluxor.getCoefficient( 990,990,1000,1000)
	print "Should be .4444 ", fluxor.getCoefficient( 1000,1000,1000,1000)
	print "Should be 1 ", fluxor.getCoefficient( 1010,1010,1000,1000)
	print "Should be 1 ", fluxor.getCoefficient( 1020,1020,1000,1000)
	print "Should be 1 ", fluxor.getCoefficient( 1030,1030,1000,1000)
	print "Should be .4444 ", fluxor.getCoefficient( 1040,1040,1000,1000)
	print "Should be .1111 ", fluxor.getCoefficient( 1050,1050,1000,1000)
	print "Should be 0 ", fluxor.getCoefficient( 1060,1060,1000,1000)
	
if __name__ == '__main__':
	#a = 1
	showGui()
	#test_projection()
	#test_flux()
	#generate_origin_points_citrus()
