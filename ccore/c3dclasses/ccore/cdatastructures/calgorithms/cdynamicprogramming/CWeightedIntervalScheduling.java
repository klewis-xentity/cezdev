//-------------------------------------------------------
// file: CWeightedIntervalScheduling
// desc: 
//-------------------------------------------------------
import c3dclasses.*;

//--------------------------------------------------------
// name: CWeightedIntervalScheduling
// desc: 
//--------------------------------------------------------
public class CWeightedIntervalScheduling {
	protected CArray m_intervals;
	public CWeightedIntervalScheduling(CArray intervals) { this.m_intervals = intervals; }
	public CWeightedIntervalScheduling() { this.m_intervals = __.carray(); }	
	public void addInterval(int s, int f, int v) { this.m_intervals.push(__.chash("s",s,"f",f,"v",v)); }
	public CHash getInterval(int i) { return this.m_intervals._chash(i); }
	public int size() { return this.m_intervals.length(); }
	public String toString() { return this.m_intervals.toString(); }
	public void sortIntervalsByFinishTime() {
		m_intervals.sort(new CFunction() {
			public CReturn call(CArray args) { 
				return CReturn._done(__.cmp(args._chash(0)._int("f"),args._chash(1)._int("f")));
			} // end call()
		}); // end sort()
	} // end sortSchedulesByFinishTime()
	public CArray computeLatestCompatibleIntervals() {
		this.sortIntervalsByFinishTime();
		int n = this.m_intervals.length();
		CArray P = __.carray_c(n);
		CArray I = this.m_intervals;
		for(int i=0; i<n; i++) {
			P._(i,-1);
			for(int j=i-1; j>-1; j--) {
				if(this.compatible(i,j)) {	
					P._(i,j);
					break;
				} // end if
			} // end for
		} // end for
		return P;
	} // computeLatestNonConflictingIntervals()
	public boolean compatible(int aIndex, int bIndex) { return this.compatible(this.m_intervals._chash(aIndex),this.m_intervals._chash(bIndex)); }
	public boolean compatible(CHash a, CHash b) { return (a._int("f") <= b._int("s") && b._int("f") >= a._int("s")) || (b._int("f") <= a._int("s") && a._int("f") >= b._int("s")); }
	
	//-----------------------------------------------------------------------
	// name: computeOPT_bf()
	// desc: computes optimum value using bruteforce recursive approach
	//-----------------------------------------------------------------------
	int computeOPT_bf() { return this.computeOPT_bf(this.m_intervals.length()-1, this.computeLatestCompatibleIntervals()); }
	int computeOPT_bf(int j, CArray P) {
		if(j<=-1)
			return 0;
		int Vj = this.getInterval(j)._int("v");	
		int Pj = P._int(j);	
		return Math.max(Vj + computeOPT_bf(Pj, P), computeOPT_bf(j-1, P)); 
	} // end computeOpt_bf()
	
	//-----------------------------------------------------------------------
	// name: computeOPT_itr()
	// desc: computes optimum value using bruteforce recursive approach
	//-----------------------------------------------------------------------
	CWeightedIntervalScheduling computeOPTSolution_itr() { 
		CArray intervals = __.carray();
		int value = this.computeOPT_itr(this.computeLatestCompatibleIntervals(), intervals);
		return new CWeightedIntervalScheduling(intervals);
	} // end computeOPTSolution_itr()
	
	int computeOPT_itr() { return this.computeOPT_itr(this.computeLatestCompatibleIntervals(), null); }
	int computeOPT_itr(CArray P, CArray S) {
		CArray OPT = __.carray_c(this.m_intervals.length());
		int n = OPT.length();
		OPT._(0,this.getInterval(0)._int("v"));
		for(int j=1; j<n; j++) {
			int Vj = this.getInterval(j)._int("v");	
			int Pj = P._int(j);
			int OPTj = Math.max(Vj + OPT._int(Pj), OPT._int(j-1));
			OPT._(j,OPTj);
		} // end for
		
		__.println(OPT);
		
		return OPT._int(n-1);
	} // end computeOpt_itr()
	
	
	
	
	
	/*
	public int maximumOptimalSchedule() {
		int j = this.m_intervals.length();
		CArray M = __.carray_c(j);
		CArray P = this.computeLatestNonConflictingIntervals();
		return this.maximumOptimalSchedule_rec(j-1, P, M);
	} // end maximumOptimalSchedule()
	
	
	public int maximumOptimalSchedule() {
		int j = this.m_intervals.length();
		CArray M = __.carray_c(j);
		CArray P = this.computeLatestNonConflictingIntervals();
		for(int i=0; i<j; i++) {
			if(M._int(i
		} // end for
	
	} // end maximumOptimalSchedule()
	
	
	public int maximumOptimalSchedule_rec(int j, CArray P, CArray M) { 
		if(j == 0) 
			return 0;
		else if(M.exist(j)) 
			return M._int(j); 
		int vj = this.getInterval(j)._int("v");
		int mj = Math.max(vj + maximumOptimalSchedule_rec(P._int(j), P, M), 
							maximumOptimalSchedule_rec(j-1, P, M));
		M._(j,mj);
		return M._int(j); 
	} // end maximumOptimalSchedule()
	
	public CWeightedIntervalScheduling minimalOptimalSchedule() { return null; }
	*/
	
		
	//-------------------------------------------------------
	// name: main()
	// desc: 
	//-------------------------------------------------------
	public static void main(String[] args) {
		CWeightedIntervalScheduling cwis = new CWeightedIntervalScheduling();
		/*
		cwis.addInterval(0,3,2);
		cwis.addInterval(1,5,4);
		cwis.addInterval(4,6,4);
		cwis.addInterval(2,8,7);
		cwis.addInterval(6,9,2);
		cwis.addInterval(7,10,1);
		*/
		
		
		cwis.addInterval(1,2,50); 
		cwis.addInterval(3,5,20);
		cwis.addInterval(6,19,100);
		cwis.addInterval(2,100,200);
		
		
		__.println(cwis);
		
		cwis.sortIntervalsByFinishTime();
		__.println(cwis);
	
		
		/*
		__.println(cwis.compatible(3,0));
		__.println(cwis.compatible(0,1));
		__.println(cwis.compatible(1,2));
		__.println(cwis.compatible(3,2));
		*/
		
		__.println(cwis.computeLatestCompatibleIntervals());
		
		
		//__.println(cwis.computeOPT_bf());
		__.println(cwis.computeOPT_itr());
		//__.println(cwis.computeOPTSolution_itr());
		
		
		//__.println(cwis.maximumOptimalSchedule());
		
		
		
	//	CFunction n2 = new CFunction("n2") {
	//		public CReturn call(CArray args) {
	//			int x = args._int(0);
	//			return CReturn._done(x*x);
	//		} // end call()
	//	}; // end CFunction
	//	__.alert("The value of the int is: " + n2.call(__.args(9))._int());
	} // end main()
} // end CWeightedIntervalScheduling
