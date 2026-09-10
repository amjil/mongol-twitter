import 'package:flutter/widgets.dart';

class PostFrameTrigger extends StatefulWidget {
  final VoidCallback onLayout;
  final Widget child;

  const PostFrameTrigger({
    super.key,
    required this.onLayout,
    required this.child,
  });

  @override
  State<PostFrameTrigger> createState() => _PostFrameTriggerState();
}

class _PostFrameTriggerState extends State<PostFrameTrigger> {
  @override
  void initState() {
    super.initState();
    // Safely registers the callback exactly once when the widget mounts
    WidgetsBinding.instance.addPostFrameCallback((_) {
      widget.onLayout();
    });
  }

  @override
  Widget build(BuildContext context) {
    return widget.child;
  }
}
