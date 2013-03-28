package com.seboid.udemcalendrier;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import android.util.Log;

//
// test d'un filter stream
//
// elimine tout ce qui est entre le from et le to
// par exemple: filtre <img  ...... >
// <img src='data:image/png;base64,iVBORw0KGg...TkSuQmCC' border='0' alt='' />
//
// pas implante avec des ring buffers, alors pas tres performant... mais bon.
//
public class myFilterInputStream extends FilterInputStream {

	int nbRead1;
	//	int nbRead;

	boolean searchFrom; // true -> search for from pattern, false -> searching for to pattern
	byte[] from;
	byte[] to;

	byte[] buffrom;
	byte[] bufto;
	int nb; // nb bytes in buffer


	protected myFilterInputStream(InputStream in,String startPattern,String endPattern) {
		super(in);
		//	nbRead=0;
		nbRead1=0;
		try {
			from=startPattern.getBytes("UTF-8");
			to=endPattern.getBytes("UTF-8");
			buffrom=new byte[from.length];
			bufto=new byte[to.length];
		} catch (UnsupportedEncodingException e) { }
		searchFrom=true;
		nb=0;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		super.close();
		Log.d("filter","read "+nbRead1+" bytes");
	}

	
	@Override
	public int read() throws IOException {
		nbRead1++;
		int i;

		if( searchFrom ) {
			// le buffer doit etre rempli de la bonne taille
			while( nb<from.length ) {
				int c=super.read();
				if( c<0 ) break;
				buffrom[nb]=(byte)c;
				nb++;
			}
			if( nb!=from.length || !Arrays.equals(buffrom,from)) {
				// eof. output first char and move the buffer left
				if( nb==0 ) return -1;
				byte k=buffrom[0];
				for(i=1;i<nb;i++) buffrom[i-1]=buffrom[i];
				nb--;
				return k;
			}
			// un match!!!!!
			searchFrom=false;
			// remplace plutot que vide le buffer
			// normalement on vide le buffer... attention
			//for(i=0;i<nb;i++) buf[i]=(byte)'x';
			nb=0; // flush le buffer. On repart a zero.
		}
		// on cherche un to et on skip tant qu'on a pas trouve
		for(;;) {
			// on cherche le to
			// le buffer doit etre rempli de la bonne taille
			while( nb<to.length ) {
				int c=super.read();
				if( c<0 ) break;
				bufto[nb]=(byte)c;
				nb++;
			}
			if( nb!=to.length || !Arrays.equals(bufto,to) ) {
				// eof. output first char and move the buffer left
				if( nb==0 ) return -1;
				byte k=bufto[0];
				for(i=1;i<nb;i++) bufto[i-1]=bufto[i];
				nb--;
				continue; // on skip et on tente le prochain char
				//return (byte)'y'; // on remplace par un autre char... normalement on skip
			}
			// un match!!!!!
			searchFrom=true;
			// remplace plutot que vide le buffer
			// normalement on vide le buffer... attention
			//for(i=0;i<nb;i++) buf[i]=(byte)'x';
			nb=0; // on vide le buffer parce qu' on skip son contenu
			break;
		}

		return read();
	}

	@Override
	public int read(byte[] buffer, int offset, int count)
			throws IOException {
		//		nbRead++;
		//			int k=super.read(buffer, offset, count);

		if( count==0 ) return 0;

		int i;
		int c;
		for(i=0;i<count;i++) {
			c=read();
			if( c<0 ) break;
			buffer[offset+i]=(byte)c;
		}
		if( i==0 ) return(-1); // eof
		return i; // devrait toujours etre >0, en fait =count
	}

}
