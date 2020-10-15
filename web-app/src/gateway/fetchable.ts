export interface Fetchable<Subject> {
    status: "unrequested" | "pending" | "succeeded" | "failed"
    value: Subject | null
    errorMessage: string | null
}
