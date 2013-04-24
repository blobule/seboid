package com.seboid.udembottin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

public class MainActivity extends Activity {
	AutoCompleteTextView tvNom;
	AutoCompleteTextView tvTel;
	TextView status;
	CheckBox etudiants;
	WebView web;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// doLDAP();

		status = (TextView) findViewById(R.id.status);
		etudiants = (CheckBox) findViewById(R.id.etudiants);

		web = (WebView) findViewById(R.id.infopersonne);
		web.setScrollContainer(true);
		web.setScrollbarFadingEnabled(false);
		web.setBackgroundColor(0xff000000);
		web.getSettings().setJavaScriptEnabled(false);

		tvNom = (AutoCompleteTextView) findViewById(R.id.auto_nom);
		tvTel = (AutoCompleteTextView) findViewById(R.id.auto_tel);

		/** Get the list of the months */
		// String[] months =
		// getResources().getStringArray(R.array.months_array);
		/** Create a new ArrayAdapter and bind list_item.xml to each list item */
		// final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		// R.layout.list_item, months);
		AutoCompleteAdapter adapterNom = new AutoCompleteAdapter(this, "cn");
		AutoCompleteAdapter adapterTel = new AutoCompleteAdapter(this,
				"telephonenumber");

		/** Associate the adapter with textView */
		tvNom.setAdapter(adapterNom);
		tvTel.setAdapter(adapterTel);

		// tv.addTextChangedListener(new TextWatcher() {
		//
		// @Override
		// public void afterTextChanged(Editable arg0) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public void beforeTextChanged(CharSequence arg0, int arg1,
		// int arg2, int arg3) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// @Override
		// public void onTextChanged(CharSequence s, int start, int before,
		// int count) {
		// // adapter.add(">"+count+"<");
		// // MainActivity.this.tv.getAdapter().add("yoyo");
		// Log.d("text", ">" + s + "<" + start + ":" + before + ":"
		// + count);
		// // if( s.charAt(start)=='b' ) adapter.add("bonjour");
		// }
		// });

		tvNom.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int pos,
					long id) {
				Log.d("bottin", "click pos=" + pos);
				//
				// affiche!
				//
				Personne p = (Personne) adapter.getItemAtPosition(pos);
				web.loadDataWithBaseURL(null, p.toHtml(), "text/html", "utf-8",
						null);
			}
		});
		tvTel.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int pos,
					long id) {
				Log.d("bottin", "click pos=" + pos);
				//
				// affiche!
				//
				Personne p = (Personne) adapter.getItemAtPosition(pos);
				web.loadDataWithBaseURL(null, p.toHtml(), "text/html", "utf-8",
						null);

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	//
	// Personne
	//

	private class Personne implements Comparable {
		String cn;
		String title;
		String telephonenumber; // 514 343-6852
		String roomnumber; // 2391
		String buildingname; // PAVILLON ANDRE-AISENSTADT
		String ou; // Faculté des arts et des sciences - Département
		// labeleduri =
		String mail; // sebastien.3d.roy@umontreal.ca

		// givenname = Sébastien
		// sn = Roy

		// le tostring donne la string a mettre dans le text entry
		@Override
		public String toString() {
			return cn;
		}

		public int compareTo(Personne p) {
			return cn.compareToIgnoreCase(p.cn);
		}

		@Override
		public int compareTo(Object p) {
			return this.compareTo((Personne) p);
		}

		public String toHtml() {
			String res="<style type=\"text/css\">body { color:" + "#ffffff"
					+ "; background-color:" + "#000000" + " } a { color:"
					+ "#8080ff" + "; } h2 { color:" + "#ffffff"
					+ "; } </style><body>";
			res+= "<font size=\"+2\">" + cn + "</font><br/>";
			res+= title + "<br/>";
			if( ou!=null ) res+= "<p>"+ou + "</p>";
			if( telephonenumber!=null ) res+= "tél: "+telephonenumber + "<br/>";
			if( buildingname!=null ) {
				res+= "<p>"+buildingname;
				if( roomnumber!=null ) res+= "<br/>"+roomnumber;
					res+="</p>";
			}
			if( mail!=null ) res+= "<p>" + mail + "</p>";
			res+="</body>";
			return res;
		};
	}

	private class AutoCompleteAdapter extends ArrayAdapter<Personne> implements
			Filterable {

		private LayoutInflater mInflater;
		private boolean tooMany; // true -> too many results returned to ldap.
									// getCount will be 0.
		private String field; // "cn", ... pour le LDAP search

		public AutoCompleteAdapter(final Context context, String field) {
			super(context, -1);
			mInflater = LayoutInflater.from(context);
			this.field = field;
			tooMany = false;
		}

		@Override
		public View getView(final int position, final View convertView,
				final ViewGroup parent) {
			final LinearLayout layout;
			TextView tvNom, tvTitre;
			if (convertView != null) {
				layout = (LinearLayout) convertView;
			} else {
				layout = (LinearLayout) mInflater.inflate(R.layout.rangee2,
						parent, false);
			}
			tvNom = (TextView) layout.findViewById(R.id.nom);
			tvTitre = (TextView) layout.findViewById(R.id.titre);

			Personne p = getItem(position);
			tvNom.setText(p.cn);
			tvTitre.setText(p.title);

			return layout;
		}

		@Override
		public void notifyDataSetChanged() {
			// TODO Auto-generated method stub
			super.notifyDataSetChanged();
			Log.d("bottin", "dataset changed!");
			showSomeResults();
		}

		@Override
		public void notifyDataSetInvalidated() {
			// TODO Auto-generated method stub
			super.notifyDataSetInvalidated();
			Log.d("bottin", "dataset invalidated!");
		}

		public void showNoResult() {
			Log.d("publish", "got no result!");
			// status.setImageDrawable(getResources().getDrawable(R.drawable.beer));
			status.setText("( 0 )");
		}

		public void showTooManyResult() {
			Log.d("publish", "got too many result!");
			// status.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
			status.setText("( >30 )");
		}

		public void showSomeResults() {
			Log.d("publish", "got some results!");
			// status.setImageDrawable(getResources().getDrawable(R.drawable.martini));
			status.setText("( " + getCount() + ")");
		}

		@Override
		public Filter getFilter() {
			Filter myFilter = new Filter() {
				@Override
				protected FilterResults performFiltering(CharSequence s) {
					if (s == null) {
						Log.d("filter", "no constraint");
						return null;
					}

					final FilterResults filterResults = new FilterResults();

					Log.d("filter", "constraint=" + s);
					if (s.length() <= 2) {
						filterResults.values = null;
						filterResults.count = -2; // too many!
						return filterResults; // au moins 1 char pour une
												// recherche
					}

					// trouve dans le bottin!
					List<Personne> vals;
					try {
						vals = doLDAP(s.toString(), field);
						filterResults.values = vals;
						filterResults.count = vals.size();
					} catch (NoResultException e) {
						filterResults.values = null;
						filterResults.count = -1;
					} catch (TooManyResultsException e) {
						filterResults.values = null;
						filterResults.count = -2;
					}
					return filterResults;
				}

				@SuppressWarnings("unchecked")
				@Override
				protected void publishResults(CharSequence s, FilterResults res) {
					Log.d("bottin", "publich results");
					if (res == null) {
						notifyDataSetInvalidated();
						showNoResult();
						return;
					}

					if (res.count == -1 || res.count == 0) {
						showNoResult();
					} else if (res.count == -2) {
						showTooManyResult();
					}

					if (res.values == null) {
						notifyDataSetInvalidated();
						return;
					}

					// check noresult/toomany
					List<Personne> plist = (List<Personne>) res.values;

					// change l'adapter pour refleter le resultat
					clear();
					for (Personne v : (List<Personne>) res.values) {
						add(v);
					}
					if (res.count > 0) {
						notifyDataSetChanged();
					} else {
						notifyDataSetInvalidated();
					}
				}
			};
			return myFilter;
		}
	}

	@SuppressWarnings("serial")
	class NoResultException extends Exception {
	};

	@SuppressWarnings("serial")
	class TooManyResultsException extends Exception {
	};

	//
	// recherche LDAP
	// field: "cn", ...
	//
	public List<Personne> doLDAP(String s, String field)
			throws NoResultException, TooManyResultsException {
		try {

			LDAPConnection ldap = new LDAPConnection("bottin.umontreal.ca", 389);

			// Log.d("ldap", "connected");

			// String filter = "(cn=roy sebastien)";
			// String filter="(telephonenumber=*6852*)";
			// String filter="(uid=425780)";
			// String filter = "(cn=" + s + "*)";
			// String filter = "(|(cn=" + s + "*)(telephonenumber=*"+s+"*))";
			// String filter = "(telephonenumber=*"+s+"*)";
			// cherche pour *s*, en realite...

			// cherche pour un mot (nom ou prenom) qui FINIT par s
			// String filter = "(cn=*" + s + ")";

			// cherche pour un mot qui COMMENCE par s
			// String filter = "(cn=" + s + "*)";

			// cherche pour un mot qui CONTIENT s (plus lent)
			String filter = "(" + field + "=*" + s + "*)";

			// (dn='uid=425780, ou=personnel, ou=people, dc=UMontreal, dc=CA')
			// telephonenumber = 514 343-6852
			// roomnumber = 2391
			// buildingname = PAVILLON ANDRE-AISENSTADT
			// ou = Faculté des arts et des sciences - Département
			// d'informatique et de recherche opérationnelle
			// title = Professeur agrégé
			// labeleduri =
			// mail = sebastien.3d.roy@umontreal.ca
			// givenname = Sébastien
			// sn = Roy
			// cn = Roy Sébastien

			SearchRequest request = new SearchRequest("", SearchScope.SUB,
					filter);
			request.setSizeLimit(100);
			request.setTimeLimitSeconds(30);

			SearchResult result;

			try {
				Log.d("ldap", "searching " + field);
				result = ldap.search(request);
			} catch (LDAPSearchException lse) {
				result = lse.getSearchResult();
				Log.d("LDAP",
						"(too many) search exception " + result.getEntryCount());
				throw new TooManyResultsException();
			}

			if (result.getResultCode() == ResultCode.SUCCESS) {
				int k = result.getEntryCount();
				Log.d("ldap", "got " + k + " entries");

				List<Personne> res = new ArrayList<Personne>();

				if (k == 0) {
					Log.d("ldap", "no results");
					throw new NoResultException();
				}

				boolean etud = etudiants.isChecked();

				List<SearchResultEntry> se = result.getSearchEntries();
				for (SearchResultEntry sre : se) {
					// Log.d("ldap", "------");
					Collection<Attribute> att = sre.getAttributes();
					if (!etud
							&& sre.getAttributeValue("title").matches(
									"Étudiant.*"))
						continue; // skip etudiants

					Personne p = new Personne();
					p.cn = sre.getAttributeValue("cn");
					p.title = sre.getAttributeValue("title");
					p.telephonenumber = sre
							.getAttributeValue("telephonenumber");
					p.roomnumber = sre.getAttributeValue("roomnumber");
					p.buildingname = sre.getAttributeValue("buildingname");
					p.ou = sre.getAttributeValue("ou");
					p.mail = sre.getAttributeValue("mail");

					res.add(p);
				}
				Collections.sort(res);
				return res;
			} else {
				Log.d("ldap", "got ... failed.");
			}
			ldap.close();
			return null;
		} catch (LDAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

}
