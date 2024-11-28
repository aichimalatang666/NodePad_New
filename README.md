
本项目实现了基本功能（时间戳和搜索功能）、扩展功能（UI美化和背景色修改）
## 1. 基本功能
### 1.1 时间戳
#### 1.1.1 步骤一：在NotesList类的private static final String[] PROJECTION = new String[]{}中增加 NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE，使其在后面的搜索中才能从SQLite中读取修改时间的字段。
 ```
private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE 
    };
```
#### 1.1.2 步骤二：修改适配器内容，增加dataColumns中装配到ListView的内容
```
 String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE ,NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE} ;
 int[] viewIDs = { android.R.id.text1 ,R.id.text2 };
```
#### 1.1.3 步骤三：在layout文件夹中的noteslist_item.xml增加一个textview组件
```
<TextView
        android:id="@+id/text2"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:textAppearance="?android:attr/textAppearanceLarge"
        android:textSize="12dp"
        android:gravity="center_vertical"
        android:paddingLeft="10dip"
        android:singleLine="true"
        android:layout_weight="1"
        android:layout_margin="0dp"
        />
```
#### 1.1.4 步骤四：在NoteEditor修改updateNote方法中的时间类型
```
 private final void updateNote(String text, String title) {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String dateFormat = simpleDateFormat.format(date);
        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, dateFormat);
```
#### 1.1.5 功能展示

### 1.2 搜索功能
#### 1.2.1 步骤一：在layout文件夹中的list_options_menu.xml增加搜索功能
```
<item
        android:id="@+id/menu_search"
        android:icon="@android:drawable/ic_search_category_default"
        android:showAsAction="always"
        android:title="search">
    </item>
```
#### 1.2.2 步骤二：新建一个查找笔记内容的布局文件note_search.xml
```
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <SearchView
        android:id="@+id/search"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:iconifiedByDefault="false"
        android:queryHint="查找" />
    <ListView
        android:id="@+id/list"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</LinearLayout>
```
#### 1.2.3 步骤三：在NotesList.java中的onOptionsItemSelected方法中添加search查询的处理
```
case R.id.menu_search:
                Intent intent = new Intent();
                intent.setClass(this, NoteSearch.class);
                this.startActivity(intent);
                return true;
```
#### 1.2.4 步骤四：新建一个NoteSearch类用于search功能的功能实现
```
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class NoteSearch extends Activity implements SearchView.OnQueryTextListener
{
    ListView listView;
    SQLiteDatabase sqLiteDatabase;
    private static final String[] PROJECTION = new String[]{
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE//时间
    };
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(this, "您选择的是："+query, Toast.LENGTH_SHORT).show();
        return false;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_search);
        SearchView searchView = (SearchView) findViewById(R.id.search);
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }
        listView = (ListView) findViewById(R.id.list);
        sqLiteDatabase = new NotePadProvider.DatabaseHelper(this).getReadableDatabase();
        //设置该SearchView显示搜索按钮
        searchView.setSubmitButtonEnabled(true);
        //设置该SearchView内默认显示的提示文本
        searchView.setQueryHint("查找");
        searchView.setOnQueryTextListener(this);

    }
    public boolean onQueryTextChange(String string) {
        // 查询条件：标题或笔记内容包含输入的字符串
        String selection1 = NotePad.Notes.COLUMN_NAME_TITLE + " like ? or " + NotePad.Notes.COLUMN_NAME_NOTE + " like ?";
        String[] selection2 = {"%" + string + "%", "%" + string + "%"};

        // 执行查询操作
        Cursor cursor = sqLiteDatabase.query(
                NotePad.Notes.TABLE_NAME,   // 查询的表名
                PROJECTION,                 // 返回查询结果的列
                selection1,                 // 查询条件
                selection2,                 // 查询条件对应的值
                null,                       // 不对查询结果进行分组
                null,                       // 不对查询结果进行行组过滤
                NotePad.Notes.DEFAULT_SORT_ORDER // 默认的排序方式
        );

        // 定义要显示的游标列名，这里初始化为标题列
        String[] dataColumns = {
                NotePad.Notes.COLUMN_NAME_TITLE,    // 标题列
                NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE  // 修改日期列
        };

        // 定义显示游标列的视图ID，这里初始化为noteslist_item.xml中的TextView
        int[] viewIDs = {
                android.R.id.text1,  // 显示标题的TextView
                android.R.id.text2   // 显示修改日期的TextView
        };

        // 创建适配器，将游标数据绑定到ListView
        SimpleCursorAdapter adapter
                = new SimpleCursorAdapter(
                this,                             // 上下文，ListView的父类
                R.layout.noteslist_item,         // 指定列表项的布局文件
                cursor,                          // 查询结果游标
                dataColumns,                     // 显示的数据列
                viewIDs                          // 对应视图ID
        );

        // 设置ListView的适配器为刚刚创建的CursorAdapter
        listView.setAdapter(adapter);

        return true;
    }
}
```
#### 1.2.5 步骤五：在清单文件AndroidManifest.xml里面注册NoteSearch,在NotesList下面添加：
```
<activity android:name="NoteSearch" android:label="@string/menu_search">
```
#### 1.2.6 功能展示

## 2. 扩展功能
### 2.1 UI美化
#### 2.1.1 步骤一：将NotesList和NoteSearch的颜色从黑色换成白色，在AndroidManifest.xml中NotesList和NoteSearch的Activity中添加：
```
<activity android:name="NotesList" android:label="@string/title_notes_list"
            android:theme="@android:style/Theme.Holo.Light">
```
```
<activity android:name="NoteSearch" android:label="@string/menu_search"
            android:theme="@android:style/Theme.Holo.Light"/>
```
#### 2.1.2 步骤二：在NotePad类中的public static final class Notes implements BaseColumns{}中添加：
```
public static final String COLUMN_NAME_BACK_COLOR = "color";
 public static final int DEFAULT_COLOR = 0; //白色
        public static final int YELLOW_COLOR = 1; //黄色
        public static final int BLUE_COLOR = 2; //蓝色
        public static final int GREEN_COLOR = 3; //绿色
        public static final int RED_COLOR = 4; //红色
```
#### 2.1.3 步骤三：在NotePadProvider类中的public void onCreate(SQLiteDatabase db) {}中添加：
```
+ NotePad.Notes.COLUMN_NAME_BACK_COLOR + " INTEGER"
```
#### 2.1.4 步骤四：在NotePadProvider.java中添加对其相应的处理，
#### 在static中添加：
```
 sNotesProjectionMap.put(
                NotePad.Notes.COLUMN_NAME_BACK_COLOR,
                NotePad.Notes.COLUMN_NAME_BACK_COLOR);
```

#### 在insert中
```
if (values.containsKey(NotePad.Notes.COLUMN_NAME_BACK_COLOR) == false) {
            values.put(NotePad.Notes.COLUMN_NAME_BACK_COLOR, NotePad.Notes.DEFAULT_COLOR);
        }
```
#### 2.1.5 步骤五：创建MyCursorAdapter类
```
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.widget.SimpleCursorAdapter;

public class MyCursorAdapter extends SimpleCursorAdapter {
    public MyCursorAdapter(Context context, int layout, Cursor c,
                           String[] from, int[] to) {
        super(context, layout, c, from, to);
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor){
        super.bindView(view, context, cursor);
        //从从数据库读取的游标中获取与笔记列表对应的颜色数据，并设置笔记颜色
        int x = cursor.getInt(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_BACK_COLOR));
        switch (x){
            case NotePad.Notes.DEFAULT_COLOR:
                view.setBackgroundColor(Color.rgb(255, 255, 255));
                break;
            case NotePad.Notes.YELLOW_COLOR:
                view.setBackgroundColor(Color.rgb(247, 216, 133));
                break;
            case NotePad.Notes.BLUE_COLOR:
                view.setBackgroundColor(Color.rgb(165, 202, 237));
                break;
            case NotePad.Notes.GREEN_COLOR:
                view.setBackgroundColor(Color.rgb(161, 214, 174));
                break;
            case NotePad.Notes.RED_COLOR:
                view.setBackgroundColor(Color.rgb(244, 149, 133));
                break;
            default:
                view.setBackgroundColor(Color.rgb(255, 255, 255));
                break;
        }
    }
}
```
#### 2.1.6 步骤六：在NotesList.java中的PROJECTION添加颜色项：
```
 private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            //Extended:display time, color
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, // 2
            NotePad.Notes.COLUMN_NAME_BACK_COLOR
    };
```
#### 2.1.7 步骤七：将NotesList.java中用的SimpleCursorAdapter改为使用MyCursorAdapter：
```
MyCursorAdapter adapter = new MyCursorAdapter(
                this,
                R.layout.noteslist_item,
                cursor,
                dataColumns,
                viewIDs
        );
```
#### 2.1.8 功能展示

### 2.2 背景色修改
#### 2.2.1 步骤一：在NotesList.java中为PROJECTION中添加：
```
  NotePad.Notes.COLUMN_NAME_BACK_COLOR
```

#### 2.2.2 步骤二：在editor_options_menu.xml中添加一个更改背景的功能选项:
```
<item android:id="@+id/menu_color"
        android:title="@string/menu_color"
        android:icon="@drawable/ic_menu_edit"
        android:showAsAction="always"/>
```
#### 2.2.3 步骤三：在NoteEditor类中的onOptionsItemSelected方法中的switch中添加:
```
 case R.id.menu_revert:
            cancelNote();
            break;
            case R.id.menu_color:
                changeColor();
                break;
```
#### 2.2.4 步骤四：在NoteEditor类中添加函数changeColor：
```
private final void changeColor() {
        Intent intent = new Intent(null,mUri);
        intent.setClass(NoteEditor.this,NoteColor.class);
        NoteEditor.this.startActivity(intent);
    }
```
#### 2.2.5 步骤五：新建布局note_color.xml，水平线性布局放置6个ImageButton，对选择颜色界面进行布局：
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <ImageButton
        android:id="@+id/color_white"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="@color/colorWhite"
        android:onClick="white"/>
    <ImageButton
        android:id="@+id/color_yellow"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="@color/colorYellow"
        android:onClick="yellow"/>
    <ImageButton
        android:id="@+id/color_blue"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="@color/colorBlue"
        android:onClick="blue"/>
    <ImageButton
        android:id="@+id/color_green"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="@color/colorGreen"
        android:onClick="green"/>
    <ImageButton
        android:id="@+id/color_red"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="@color/colorRed"
        android:onClick="red"/>
    <ImageButton
        android:id="@+id/color_purple"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="@color/colorPurple"
        android:onClick="purple"/>
</LinearLayout>
```
新建文件资源color.xml，添加所需要的颜色：
```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="colorWhite">#fff</color>
    <color name="colorYellow">#FFEAA5</color>
    <color name="colorBlue">#B1D9FD</color>
    <color name="colorGreen">#D5FFDE</color>
    <color name="colorRed">#FFCDC6</color>
    <color name="colorPurple">#D8C7FB</color>
</resources>
```
#### 2.2.6 新建NoteColor类，用来选择颜色：
```
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class NoteColor extends Activity {
    private Cursor mCursor;
    private Uri mUri;
    private int color;
    private static final int COLUMN_INDEX_TITLE = 1;
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_BACK_COLOR, // 背景颜色列
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_color);
        // 从 NoteEditor 中传入的 URI
        mUri = getIntent().getData();
        mCursor = managedQuery(
                mUri,        // 要检索的笔记 URI
                PROJECTION,  // 要检索的列
                null,        // 不使用选择条件，因此不需要 Where 列
                null,        // 不使用 Where 值
                null         // 不需要排序
        );
    }

    @Override
    protected void onResume() {
        // 执行顺序在 onCreate 后
        if (mCursor != null) {
            mCursor.moveToFirst();
            color = mCursor.getInt(COLUMN_INDEX_TITLE); // 获取当前笔记的背景颜色
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        // 在 finish() 后，保存所选颜色到数据库
        super.onPause();
        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_BACK_COLOR, color); // 更新背景颜色
        getContentResolver().update(mUri, values, null, null); // 更新数据库中的颜色
    }

    public void white(View view) {
        color = NotePad.Notes.DEFAULT_COLOR; // 设置为默认颜色
        finish(); // 结束当前活动
    }

    public void yellow(View view) {
        color = NotePad.Notes.YELLOW_COLOR; // 设置为黄色
        finish(); // 结束当前活动
    }

    public void blue(View view) {
        color = NotePad.Notes.BLUE_COLOR; // 设置为蓝色
        finish(); // 结束当前活动
    }

    public void green(View view) {
        color = NotePad.Notes.GREEN_COLOR; // 设置为绿色
        finish(); // 结束当前活动
    }

    public void red(View view) {
        color = NotePad.Notes.RED_COLOR; // 设置为红色
        finish(); // 结束当前活动
    }

    public void purple(View view) {
        color = NotePad.Notes.PURPLE_COLOR; // 设置为紫色
        finish(); // 结束当前活动
    }
}
```
#### 2.2.7 在清单文件AndroidManifest.xml里面注册NoteColor：
```
<activity android:name="NoteColor"
            android:theme="@android:style/Theme.Holo.Light.Dialog"
            android:label="ChangeColor"
            android:windowSoftInputMode="stateVisible"/>
```
#### 2.2.8 功能展示
