package gitHub;

public class LCSR {

	public double getDistance(String a, String b) 
	{
		int NEITHER     = 0;
		int UP          = 1;
		int LEFT        = 2;
		int UP_AND_LEFT = 3;

		int n = a.length();
		int m = b.length();

		int longest =0;

		if (n > m)
			longest=n;
		else
			longest=m;

		int S[][] = new int[n+1][m+1];
		int R[][] = new int[n+1][m+1];
		int ii, jj;

		// It is important to use <=, not <.  The next two for-loops are initialization
		for(ii = 0; ii <= n; ++ii) {
			S[ii][0] = 0;
			R[ii][0] = UP;
		}
		for(jj = 0; jj <= m; ++jj) {
			S[0][jj] = 0;
			R[0][jj] = LEFT;
		}

		// This is the main dynamic programming loop that computes the score and
		// backtracking arrays.
		for(ii = 1; ii <= n; ++ii) {
			for(jj = 1; jj <= m; ++jj) { 

				if( a.charAt(ii-1) == b.charAt(jj-1) ) {
					S[ii][jj] = S[ii-1][jj-1] + 1;
					R[ii][jj] = UP_AND_LEFT;
				}

				else {
					S[ii][jj] = S[ii-1][jj-1] + 0;
					R[ii][jj] = NEITHER;
				}

				if( S[ii-1][jj] >= S[ii][jj] ) {	
					S[ii][jj] = S[ii-1][jj];
					R[ii][jj] = UP;
				}

				if( S[ii][jj-1] >= S[ii][jj] ) {
					S[ii][jj] = S[ii][jj-1];
					R[ii][jj] = LEFT;
				}
			}
		}

		// The length of the longest substring is S[n][m]
		ii = n; 
		jj = m;
		int pos = S[ii][jj] - 1;
		char lcs[] = new char[ pos+1 ];
		int count =0;

		// Trace the backtracking matrix.
		while( ii > 0 || jj > 0 ) {
			if( R[ii][jj] == UP_AND_LEFT ) {
				ii--;
				jj--;
				lcs[pos--] = a.charAt(ii);
			}

			else if( R[ii][jj] == UP ) {
				ii--;
			}

			else if( R[ii][jj] == LEFT ) {
				jj--;
			}
			count = lcs.length;
		}
		
		double lcsr=0.0;
		lcsr = (double)count/(double)longest;
		return lcsr;
	}
}