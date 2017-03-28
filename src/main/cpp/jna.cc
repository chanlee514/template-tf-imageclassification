
#include "tensorflow/core/public/session.h"
#include "tensorflow/core/platform/env.h"

using namespace tensorflow;

extern "C" {
  // Helper for status handling
  int checkStatus(Status s, const char *message) {
    if (! s.ok()) {
      LOG(ERROR) << message << s.ToString() << "\n";
      return -1;
    }
    return 0;
  }

  Session *tfCreateSession(const char *modelPath) { 
    Session* session;
    Status status = NewSession(SessionOptions(), &session);
    if (checkStatus(status, "Wrong status: ") == -1) return NULL;

    GraphDef graphDef;
    status = ReadBinaryProto(Env::Default(), modelPath, &graphDef);
    if (checkStatus(status, "Wrong status: ") == -1) return NULL;

    status = session->Create(graphDef);
    if (checkStatus(status, "Wrong status: ") == -1) return NULL;

    return session;
  }

  int tfRunString(
    Session *session,
    const char *inputLayer,
    const char *outputLayer,
    const char *data,
    int size,
    float *result) {

    string s(data, size);
    Tensor t(DT_STRING, TensorShape({}));
    t.scalar<string>()() = s;

    const Tensor& resized_tensor = t;

    std::vector<Tensor> outputs;
    Status status = session->Run(
      {{inputLayer, resized_tensor}}, {outputLayer}, {}, &outputs);

    if (checkStatus(status, "Session failed with status: ") == -1) return -1;

    auto o = outputs[0].flat<float>();
    for (int i = 0; i < o.size(); i++) {
      result[i] = o(i);
    }
    return o.size();
  }

  int tfRunFloatArray(
    Session *session,
    const char *inputLayer,
    const char *outputLayer,
    const float *data,
    int size,
    float *result) {

    Tensor t(DT_FLOAT, TensorShape({1, size}));
    for (int i = 0; i < size ; i++)
      t.matrix<float>()(0, i) = data[i];

    const Tensor& resized_tensor = t;

    std::vector<Tensor> outputs;
    Status status = session->Run(
      {{inputLayer, resized_tensor}}, {outputLayer}, {}, &outputs);
    if (checkStatus(status, "Session failed with status: ") == -1) return -1;

    auto o = outputs[0].flat<float>();
    for (int i = 0; i < o.size(); i++) {
      result[i] = o(i);
    }
    return o.size();
  }

  void tfCloseSession(Session *session) {
    session->Close();
  }
}

