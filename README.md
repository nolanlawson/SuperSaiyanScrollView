SuperSaiyanScrollView
=====================

__Version 1.2.0__ ([changelog](https://github.com/nolanlawson/SuperSaiyanScrollView#changelog))

Super-fast, super-lightweight sectioned lists for Android.

![Screenshot][1]

*SuperSaiyanScrollView on HTC Magic (Eclair) and Galaxy Nexus (Jelly Bean)*


Author
-------
Nolan Lawson

License
--------
Apache 2.0

Installation
----------

#### Eclipse

Clone the source code:

```
git clone https://github.com/nolanlawson/SuperSaiyanScrollView.git
```

Then add the `supersaiyan-scrollview` folder as a library project dependency on your own project.  If you've never worked with an Android library before, [here's a good tutorial with screenshots](https://github.com/nolanlawson/PouchDroid/wiki/Getting-Started#wiki-importing-pouchdroid-as-a-library-project) or you can read [the official docs](http://developer.android.com/tools/projects/projects-cmdline.html#ReferencingLibraryProject).

If you use Proguard, add the following to your `proguard.cfg` (Gradle handles this automatically):

    -keep public class com.nolanlawson.supersaiyan.widget.*


#### Maven

```xml
<dependency>
   <groupId>com.nolanlawson</groupId>
   <artifactId>supersaiyan-scrollview</artifactId>
   <version>1.2.0</version>
</dependency>
```

#### Gradle

```groovy
compile 'com.nolanlawson:supersaiyan-scrollview:1.2.0@aar'
```

Motivation
------

Fast-scrolling sectioned lists are one of the most common UI patterns in Android, and yet it's still a pain to implement from scratch.  Nothing in the stock Android SDK provides this functionality.

The SuperSaiyanScrollView comes to the rescue with lightning-fast UI elements and helper functions, to make working with sectioned lists easy.

Why "Super Saiyan"?  Because:

* I made it, so I get to name it.
* It's super-fast, super-powerful, and it kicks (stock) Android's ass.

![Their power levels are definitely over 9000.](http://nolanwlawson.files.wordpress.com/2013/11/android18_gets_punched_smaller.gif)

Usage
------

In your layout XML file, add a `SuperSaiyanScrollView` around your `ListView`:

```xml
<com.nolanlawson.supersaiyan.widget.SuperSaiyanScrollView
  android:id="@+id/scroll"
  android:layout_width="match_parent"
  android:layout_height="match_parent">

  <ListView
    android:id="@android:id/list"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:scrollbars="none"
    />

</com.nolanlawson.supersaiyan.widget.SuperSaiyanScrollView>
```

(I like to set `android:scrollbars="none"`, to remove the omnipresent gray scrollbars and stick with the "fast" blue scrollbars.)

Next, wrap your existing `Adapter` (e.g. an `ArrayAdapter`) in a `SectionedListAdapter`.  The `SectionedListAdapter` uses a fluent "builder" pattern, similar to `AlertDialog.Builder`: 

```java
SectionedListAdapter<MyCoolAdapter> adapter = 
    SectionedListAdapter.Builder.create(this, myCoolAdapter)
    .setSectionizer(new Sectionizer<MyCoolListItem>(){

      @Override
      public CharSequence toSection(MyCoolListItem item) {
        return item.toSection();
      }
    })
    .sortKeys()
    .sortValues()
    .build();
```


<h3>Examples</h3>

Let's walk through some short examples, which should demonstrate the simplicity and flexibility of the `SuperSaiyanScrollView`.  The source code for these apps is included in the GitHub project, and you can download the APKs here:

* [Example #1: Countries (APK)](https://nolanlawson.s3.amazonaws.com/dist/com.nolanlawson.supersaiyan/release/1.1.1/example-countries.apk)
* [Example #2: Pokémon (APK)](https://nolanlawson.s3.amazonaws.com/dist/com.nolanlawson.supersaiyan/release/1.1.1/example-pokemon.apk)

<h3>Example #1: Countries</h3>

In this example, we have a list of countries, which we'd like to sort by continent.  The finished app looks like this:

![Screenshot](http://nolanwlawson.files.wordpress.com/2014/03/ssjn_countries.png?w=570)

We have a simple Country object:

```java
public class Country {

  private String name;
  private String continent;

  /* getters and setters ... */
  
  @Override
  public String toString() {
    return name;
  }
}
```

We use a basic `ArrayAdapter<Country>` to display the countries:

```java
ArrayAdapter<Country> adapter = new ArrayAdapter<Country>(
        this, 
        android.R.layout.simple_spinner_item, 
        countries);
```

Next, we wrap it in a `SectionedListAdapter`.  In this case, we'd like to section countries by their continent, sort the continents by name, and sort countries by name:

```java
sectionedAdapter = 
    SectionedListAdapter.Builder.create(this, adapter)
    .setSectionizer(new Sectionizer<Country>(){

      @Override
      public CharSequence toSection(Country input) {
        return input.getContinent();
      }
    })
    .sortKeys()
    .sortValues(new Comparator<Country>() {
      
      public int compare(Country lhs, Country rhs) {
        return lhs.getName().compareTo(rhs.getName());
      }
    })
    .build();
```

A `Sectionizer` is a simple callback that provides a section name for the given list item.  In your own code, this might be a `HashMap` lookup, a database query, or a simple getter (as in this example).

Notice also that the keys (i.e. the section titles) and the values (i.e. the list contents) can be sorted independently, or not sorted at all.  By default, they're sorted according to the input order.

Now, let's try to change the sections dynamically!  In the action bar, the user can switch between alphabetic sorting and continent sorting:

<a href="http://nolanwlawson.files.wordpress.com/2013/11/saiyan5.png"><img src="http://nolanwlawson.files.wordpress.com/2013/11/saiyan5.png?w=570" alt="alphabetic sorting vs. continent sorting" width="570" height="499" class="aligncenter wp-image-3057" /></a>

To do so, we first get a reference to the `SuperSaiyanScrollView`:

```java
SuperSaiyanScrollView superSaiyanScrollView = 
    (SuperSaiyanScrollView) findViewById(R.id.scroll);
```

Then, we call the following function whenever the user chooses alphabetic sorting:

```java
private void sortAz() {

  // use the built-in A-Z sectionizer
  sectionedAdapter.setSectionizer(
      Sectionizers.UsingFirstLetterOfToString);

  // refresh the adapter and scroll view
  sectionedAdapter.notifyDataSetChanged();
  superSaiyanScrollView.refresh();
}
```

Notice that the `SectionedListAdapter` and `SuperSaiyanScrollView` need to be informed whenever their content changes.

Next, when the user switches back to continent sorting, we call this function:

```java
private void sortByContinent() {

  // use the by-continent sectionizer
  sectionedAdapter.setSectionizer(new Sectionizer<Country>(){

        @Override
        public CharSequence toSection(Country input) {
          return input.getContinent();
        }
      });

  // refresh the adapter and scroll view
  sectionedAdapter.notifyDataSetChanged();
  superSaiyanScrollView.refresh();
}
```

Notice that you never need to call `adapter.sort()` or `Collections.sort()` yourself. The `SectionedListAdapter` handles everything.  And it does so without ever modifying the underlying adapter, which means that view generation is lightning-fast.

<h4>Dark theme</h4>

Don't like the light overlay?  Put on your shades and set ```myapp:ssjn_overlayTheme="dark"``` in the XML:

![Screenshot](http://nolanwlawson.files.wordpress.com/2014/03/ssjn_dark_vs_light.png?w=570)

*Black hair or light hair - the choice is yours.*

<h3>Example #2: Pokémon</h3>

This example shows off some of the advanced functionality of the `SuperSaiyanScrollView`.  We have three different sortings, the size of the overlay box changes to fit the text size, and we can dynamically hide both the overlays and the section titles.

![Screenshot](http://nolanwlawson.files.wordpress.com/2013/11/saiyan12.png?w=570)

*Alphabetic vs. by-region sorting*

First off, the size of the overlay can be configured in XML.  In this example, we start off with a single-letter alphabetic sorting, so we want the overlays to be a bit smaller than normal.

Add a namespace to the root XML tag in your layout XML:

```xml
<RelativeLayout
  ...
  xmlns:myapp="http://schemas.android.com/apk/res/com.example.example1"
  ...
  >
</RelativeLayout>
```

Next, use values prefixed with `ssjn_` to define the size of the overlay:

```xml
<com.nolanlawson.supersaiyan.widget.SuperSaiyanScrollView
  ...
  myapp:ssjn_overlaySizeScheme="normal">

  <ListView
    ...
    />

</com.nolanlawson.supersaiyan.widget.SuperSaiyanScrollView>
```

I include the built-in schemes `small` (for one letter), `normal` (for most use cases), and `large` and `xlarge` (for longer section titles).  Section titles of up to two lines (separated by `\n`) are supported.

![Screenshot](http://nolanwlawson.files.wordpress.com/2013/11/saiyan7.png?w=570)

*Small, normal, large, and xlarge overlays in my AMG Geneva app.*

If you want, you can also manually specify the font size, width, height, and text color yourself:

```xml
<com.nolanlawson.supersaiyan.widget.SuperSaiyanScrollView
  ...
  myapp:ssjn_overlayWidth="400dp"
  myapp:ssjn_overlayHeight="200dp"
  myapp:ssjn_overlayTextSize="12sp"
  myapp:ssjn_overlayTextColor="@android:color/black" >

  <ListView
    ...
    />
</com.nolanlawson.supersaiyan.widget.SuperSaiyanScrollView>
```

Now, in the Java source, we have a `PocketMonster` object:

```java
public class PocketMonster {

  private String uniqueId;
  private int nationalDexNumber;
  private String type1;
  private String type2;
  private String name;
  
  /* getters and setters */

  @Override
  public String toString() {
    return name;
  }
}
```

We have a simple `PocketMonsterAdapter` to define how the monsters are displayed in the list:

```java
public class PocketMonsterAdapter 
    extends ArrayAdapter<PocketMonster> {
  
  // Constructors...
  
  @Override
  public View getView(int pos, View view, 
      ViewGroup parent) {
    
    PocketMonster monster = 
        (PocketMonster) getItem(pos);
    
    /* Create and style the view... */

    return view;
  }
}
```

We wrap this adapter in a `SectionedListAdapter` that, by default, sections and sorts everything alphabetically:

```java
 adapter = SectionedListAdapter.Builder.create(this, subAdapter)
     .setSectionizer(Sectionizers.UsingFirstLetterOfToString)
     .sortKeys()
     .sortValues(new Comparator<PocketMonster>(){

       @Override
       public int compare(PocketMonster lhs, 
             PocketMonster rhs) {
         return lhs.getName().compareToIgnoreCase(
             rhs.getName());
       }})
     .build();
```

Notice that we call both `sortKeys()` and `sortValues()`, because we want both the section titles and the Pokémon to be ordered alphabetically.  Since `PocketMonster` does not implement `Comparable`, we defined a custom `Comparator`.

Now let's say we want to organize the Pokémon by region:

<a href="http://nolanwlawson.files.wordpress.com/2013/11/saiyan11.png"><img src="http://nolanwlawson.files.wordpress.com/2013/11/saiyan11.png?w=570" alt="Pokémon sorted by region." width="570" height="499" class="aligncenter size-large wp-image-3078" /></a>

Some quick background: Pokémon are ordered by their "national ID," an integer value that starts at 1 (Bulbasaur) and goes up to 718 (Zygarde).  Every time Nintendo releases a new generation of Pokémon games, they add about 100 new monsters, set the game in a new "region," and sell about a bazillion new Pokémon toys.

So basically, we can determine the regions from the Pokémon's ID.  We'll define a new
`Sectionizer`, which is called when the user selects "sort by region":

```java
private void sortByRegion() {
  adapter.setSectionizer(new Sectionizer<PocketMonster>() {

    @Override
    public CharSequence toSection(PocketMonster input) {
      int id = input.getNationalDexNumber();
      
      // Kanto region will appear first, followed 
      // by Johto, Hoenn, Sinnoh, Unova, and Kalos
      if (id <= 151) {
        return "Kanto (Generation 1)";
      } else if (id <= 251) {
        return "Johto (Generation 2)";
      } else if (id <= 386) {
        return "Hoenn (Generation 3)";
      } else if (id <= 493) {
        return "Sinnoh (Generation 4)";
      } else if (id <= 649) {
        return "Unova (Generation 5)";
      } else {
        return "Kalos (Generation 6)";
      }
    }
  });

  // uses the nat'l pokedex order, since 
  // that's the original input order
  adapter.setKeySorting(Sorting.InputOrder);
  adapter.setValueSorting(Sorting.InputOrder);
  scrollView.setOverlaySizeScheme(
      OverlaySizeScheme.Large);

  // refresh the adapter and scroll view
  adapter.notifyDataSetChanged();
  scrollView.refresh();
}
```

Notice that we've changed the key and value sorting to `Sorting.InputOrder`, because now we want to order Pokémon by their national IDs, which was the order the data was read in.  (A custom `Comparator` would have also done the trick.)  Additionally, we've increased the size of the overlay to accommodate the longer section text.

Now, let's say we want to organize Pokémon by type.  Each Pokémon has at least one elemental type (such as "fire" or "water"), but some have two.  Ideally we would like to list Pokémon in multiple categories, so they could appear multiple times in the list.

To do so, we will define a `MultipleSectionizer` instead of a regular `Sectionizer`:

```java
private void sortByType() {
  adapter.setMultipleSectionizer(
      new MultipleSectionizer<PocketMonster>() {

    @Override
    public Collection<? extends CharSequence> toSections(
        PocketMonster monster) {
      String type1 = monster.getType1();
      String type2 = monster.getType2();

      if (!TextUtils.isEmpty(type2)) { // two types
        return Arrays.asList(type1, type2);
      } else { // one type
        return Collections.singleton(type1);
      }
    }
  });
  adapter.setKeySorting(Sorting.Natural);
  adapter.setValueSorting(Sorting.InputOrder);
  scrollView.setOverlaySizeScheme(OverlaySizeScheme.Normal);

  // refresh the adapter and scroll view
  adapter.notifyDataSetChanged();
  scrollView.refresh();
}
```

Notice that the key sorting has again changed, this time to `Sorting.Natural`, which simply sorts alphabetically.  Value sorting has changed to `Sorting.InputOrder`, because we've decided to sort Pokémon by their national IDs.

This works as expected:

![Screenshot](http://nolanwlawson.files.wordpress.com/2013/11/saiyan9.png?w=570)

*Pokémon sorted by type*

Notice that Charizard appears in both in the "Fire" and "Flying" sections, since he has two types.

This example app also shows how you can disable the section titles or section overlays, just in case you don't like them.  These values can also be set during the `Builder` chain, using `hideSectionTitles()` and `hideSectionOverlays()`.

![Screenshot](http://nolanwlawson.files.wordpress.com/2013/11/saiyan13.png?w=570)

*Comparison of hiding overlays and hiding section titles*

New in 1.2.0!
---------

Thanks to some awesome work by [michaldarda](https://github.com/michaldarda), you can now specify a custom `SectionTitleAdapter` or layout for the `SectionTitleAdapter`. If you use a layout, it should be an XML layout resource with attribute `android:id="@+id/list_header_title"` to indicate the header text.  (The default one can be found [here](https://github.com/nolanlawson/SuperSaiyanScrollView/blob/master/supersaiyan-scrollview/res/layout/list_header.xml) if you want to just modify that.)

```java
import com.nolanlawson.supersaiyan.SectionTitleAdapter;

sectionedAdapter = SectionedListAdapter.Builder.create(this, subAdapter)
    .setSectionTitleLayout(R.layout.my_layout_id)
    // alternative version
    .setSectionTitleAdapter(new MySubclassOfSectionTitleAdapter())    
    .build();
```

New in 1.X.X!
---------

You can now use this library with a RecyclerView. Why would one want to use a RecyclerView instead of a ListView? The main reason is that RecyclerView automatically comes with (easily customizable) animations for adding and removing items from its adapter. However, RecyclerView itself does not implement any scrollbar, a fast scroll feature, or sectioning. This library now provides these features for a RecyclerView with a LinearLayoutManager.

The simplest use case is simply adding a `SuperSaiyanRecyclerView` around your `RecyclerView`:

```xml
<com.nolanlawson.supersaiyan.widget.SuperSaiyanRecyclerView
  android:id="@+id/scroll"
  android:layout_width="match_parent"
  android:layout_height="match_parent">

  <android.support.v7.widget.RecyclerView
    android:id="@android:id/list"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:scrollbars="none"
    />

</com.nolanlawson.supersaiyan.widget.SuperSaiyanRecyclerView>
```

That's it! The RecyclerView now has a scrollbar with a fast scroll mode. To add a section overlay to the fast scroll mode, you can optionally call `setSections( List<String>sectionNames, List<Integer>sectionPositions )` on your `superSaiyanRecyclerView`, where `sectionPositions` refer to the item position in your `RecyclerView.Adapter` where a new section starts. 


Details
--------

See [the original blog post][2] for some historical insight.

This project was originally derived from my own [CustomFastScrollViewDemo][3], which was based on a modification of the Android "Contacts" app.  I can no longer find the original source, nor the original author.  But kudos to you, mysterious stranger! 

Changelog
--------

* 1.2.0
  * Specify a custom list header ([#7](https://github.com/nolanlawson/SuperSaiyanScrollView/issues/7))
  * Fix for deta not being refreshed ([#8](https://github.com/nolanlawson/SuperSaiyanScrollView/issues/8))
* 1.1.1
  * Fix potential issue when there are no XML attributes ([#3][issue3]) 
* 1.1.0
  * Convert to Gradle
  * Add dark theme ([#1][issue1])
* 1.0.0
  * Initial release.



[1]: http://nolanwlawson.files.wordpress.com/2013/11/supersaiyan3.png?w=400
[2]: http://nolanlawson.com/2013/11/30/introducing-the-supersaiyanscollview-super-fast-sectioned-lists-for-android/
[3]: https://github.com/nolanlawson/CustomFastScrollViewDemo
[issue1]: https://github.com/nolanlawson/SuperSaiyanScrollView/issues/1
[issue3]: https://github.com/nolanlawson/SuperSaiyanScrollView/issues/3
