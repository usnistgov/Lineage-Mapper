// Disclaimer: IMPORTANT: This software was developed at the National
// Institute of Standards and Technology by employees of the Federal
// Government in the course of their official duties. Pursuant to
// title 17 Section 105 of the United States Code this software is not
// subject to copyright protection and is in the public domain. This
// is an experimental system. NIST assumes no responsibility
// whatsoever for its use by other parties, and makes no guarantees,
// expressed or implied, about its quality, reliability, or any other
// characteristic. We would appreciate acknowledgement if the software
// is used. This software can be redistributed and/or modified freely
// provided that any derivative works bear some notice that they are
// derived from it, and any modified versions bear some notice that
// they have been modified.


package main.java.gov.nist.isg.tracking.lib;

public class UnionFind {


  private int[] id;    // id[i] = parent of i
  private int[] sz;

  // Create an empty union find data structure with N isolated sets.
  public UnionFind(int N) {
    id = new int[N];
    sz = new int[N];
    for (int i = 0; i < N; i++) {
      id[i] = i;
      sz[i] = 1;
    }
  }


  public int root(int i) {
    // adjust the size of id, sz based on the search location i
    if (i >= id.length) {
      int[] id2 = id;
      int[] sz2 = sz;
      id = new int[id2.length * 2];
      sz = new int[sz2.length * 2];
      System.arraycopy(id2, 0, id, 0, id2.length);
      System.arraycopy(sz2, 0, sz, 0, sz2.length);
      for (int k = id2.length; k < id.length; k++) {
        id[k] = k;
        sz[k] = 1;
      }
    }

    while (i != id[i]) {
      id[i] = id[id[i]];
      i = id[i];
    }
    return i;
  }

  public boolean find(int p, int q) {
    return root(p) == root(q);
  }

  public void union(int p, int q) {
    int i = root(p);
    int j = root(q);
    if (i == j) {
      return;
    }
    if (sz[i] < sz[j]) {
      id[i] = j;
      sz[j] += sz[i];
    } else {
      id[j] = i;
      sz[i] += sz[j];
    }
  }

}
