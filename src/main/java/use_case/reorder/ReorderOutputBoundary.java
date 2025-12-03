package use_case.reorder;

public interface ReorderOutputBoundary {
    void prepareSuccessView(ReorderOutputData outputData);

    void prepareFailView(String error);
}
