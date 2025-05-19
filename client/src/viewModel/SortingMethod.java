package viewModel;

public enum SortingMethod {
    ACTIVITY("activity"),
    ALPHABETICALLY("alphabetically");

    private String methodId;
    SortingMethod(String methodId) {this.methodId = methodId;}
}
