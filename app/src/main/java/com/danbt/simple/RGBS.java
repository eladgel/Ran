package com.danbt.simple;

import java.util.ArrayList;

public class RGBS {
	private ArrayList<RGB> rgbs=new ArrayList<RGB>();

	public RGBS() {
	}

	public boolean update(int id, int r, int g, int b, int t,int maxDim) {
		for (RGB rgb : rgbs) {
			if (rgb.id == id) {
				if (rgb.match(r, g, b, t) == true) {
					return false;
				}
				rgb.r = r;
				rgb.g = g;
				rgb.b = b;
				rgb.t = t;
				if(maxDim!=-1){
				rgb.maxDim=maxDim;}
				return true;
			}
		}
		rgbs.add(new RGB(id, r, g, b, t,maxDim));
		return true;
	}

	class RGB {
		int id;
		int r;
		int g;
		int b;
		int t;
		int maxDim;

		RGB(int id, int r, int g, int b, int t,int maxDim) {
			this.id = id;
			this.r = r;
			this.g = g;
			this.b = b;
			this.maxDim = maxDim;
		}

		public boolean match(int r, int g, int b, int t) {
			if(this.r!=r){return false;}
			if(this.g!=g){return false;}
			if(this.b!=b){return false;}
			if(this.t!=t){return false;}
			return true;
		}
	}

	public int getMaxDim(int id) {
		for (RGB rgb : rgbs) {
		if (rgb.id == id) {
			return rgb.maxDim;
		}}
		return 0;
	}
}
