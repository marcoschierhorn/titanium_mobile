//
//  Appcelerator Titanium Mobile
//  WARNING: this is a generated file and should not be modified
//

#import <UIKit/UIKit.h>
#define _QUOTEME(x) #x
#define STRING(x) _QUOTEME(x)

<<<<<<< HEAD:demos/KitchenSink/build/iphone/main.m
NSString * const TI_APPLICATION_DEPLOYTYPE = @"development";
NSString * const TI_APPLICATION_ID = @"com.nolanwright.kitchensink";
=======
NSString * const TI_APPLICATION_DEPLOYTYPE = @"test";
NSString * const TI_APPLICATION_ID = @"com.appcelerator.titanium";
>>>>>>> ff7bfc4bb1dd3537d53f27d181d58d0d91c0ceb8:demos/KitchenSink/build/iphone/main.m
NSString * const TI_APPLICATION_PUBLISHER = @"nwright";
NSString * const TI_APPLICATION_URL = @"appcelerator.com";
NSString * const TI_APPLICATION_NAME = @"KitchenSink";
NSString * const TI_APPLICATION_VERSION = @"1.0";
NSString * const TI_APPLICATION_DESCRIPTION = @"No description provided";
NSString * const TI_APPLICATION_COPYRIGHT = @"2010 by nwright";
NSString * const TI_APPLICATION_GUID = @"6fe33f33fd1f4e95a06d2d217170866d";
BOOL const TI_APPLICATION_ANALYTICS = true;

#ifdef DEBUG
<<<<<<< HEAD:demos/KitchenSink/build/iphone/main.m
NSString * const TI_APPLICATION_RESOURCE_DIR = @"/Users/nwright/Appcelerator/TIDEV/titanium_mobile/demos/KitchenSink/build/iphone/build/Debug-iphonesimulator/KitchenSink.app";
=======
NSString * const TI_APPLICATION_RESOURCE_DIR = @"";
>>>>>>> ff7bfc4bb1dd3537d53f27d181d58d0d91c0ceb8:demos/KitchenSink/build/iphone/main.m
#endif

int main(int argc, char *argv[]) {
    NSAutoreleasePool * pool = [[NSAutoreleasePool alloc] init];

#ifdef __LOG__ID__
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDirectory = [paths objectAtIndex:0];
	NSString *logPath = [documentsDirectory stringByAppendingPathComponent:[NSString stringWithFormat:@"%s.log",STRING(__LOG__ID__)]];
	freopen([logPath cStringUsingEncoding:NSUTF8StringEncoding],"w+",stderr);
	fprintf(stderr,"[INFO] Application started\n");
#endif

    int retVal = UIApplicationMain(argc, argv, nil, nil);
    [pool release];
    return retVal;
}
