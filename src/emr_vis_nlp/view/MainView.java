package emr_vis_nlp.view;

/**
 * Interface which should be implemented by any class seeking to be a MainView
 * (ie, runnable top-level JFrame) for this project.
 *
 * @author alexander.p.conrad@gmail.com
 */
public interface MainView {

    public void resetAllViews();

    public void attributeSelectionChanged();

    //public void orderedAttrSelectionChanged();
    public void axisAttrSelectionChanged();

    public MainViewGlassPane getGlassPane();

    public void setSearchText(String text);
}
